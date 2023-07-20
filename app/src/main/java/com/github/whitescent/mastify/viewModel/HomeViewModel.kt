package com.github.whitescent.mastify.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import at.connyduck.calladapter.networkresult.fold
import com.github.whitescent.mastify.data.repository.AccountRepository
import com.github.whitescent.mastify.database.AppDatabase
import com.github.whitescent.mastify.network.MastodonApi
import com.github.whitescent.mastify.network.model.status.Status
import com.github.whitescent.mastify.paging.LoadState
import com.github.whitescent.mastify.paging.Paginator
import com.github.whitescent.mastify.utils.reorderedStatuses
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val timelineFetchNumber = 30

@HiltViewModel
class HomeViewModel @Inject constructor(
  private val db: AppDatabase,
  private val accountRepository: AccountRepository,
  private val api: MastodonApi,
) : ViewModel() {

  private val timelineDao = db.timelineDao()
  private var unsortedTimelineList = mutableListOf<Status>()
  private var nextPage: String? = null
  private var isInitialLoad = true

  val activeAccount get() = accountRepository.activeAccount!!
  var uiState by mutableStateOf(HomeUiState())
    private set

  private val paginator = Paginator(
    initialKey = nextPage,
    onLoadUpdated = { uiState = uiState.copy(timelineLoadState = it) },
    onRequest = { nextPage ->
      val response = api.homeTimeline(maxId = nextPage, limit = timelineFetchNumber)
      if (response.isSuccessful && !response.body().isNullOrEmpty()) {
        val body = response.body()!!
        Result.success(body)
      } else {
        Result.success(emptyList())
      }
    },
    getNextKey = {
      it.lastOrNull()?.id
    },
    onError = {
      it?.printStackTrace()
    },
    onAppend = { items, newKey ->
      unsortedTimelineList = (unsortedTimelineList + items).toMutableList()
      uiState = uiState.copy(
        timeline = reorderedStatuses(unsortedTimelineList),
        endReached = items.isEmpty()
      )
      nextPage = newKey
      db.withTransaction {
        replaceStatusRange(unsortedTimelineList)
      }
    },
    onRefresh = { items ->
      if (unsortedTimelineList.isNotEmpty()) {
        val lastStatusInApi = items.last()
        when {
          !unsortedTimelineList.any { it.id == lastStatusInApi.id } && isInitialLoad -> {
            uiState = uiState.copy(
              showNewStatusButton = true,
              newStatusCount = "$timelineFetchNumber+"
            )
            loadUnloadedStatus()
          }
          unsortedTimelineList.any { it.id == lastStatusInApi.id } &&
            !unsortedTimelineList.any { it.hasUnloadedStatus } -> {
            val newStatusList = items.filterNot {
              unsortedTimelineList.any { saved -> saved.id == it.id }
            }
            val newStatusCount = newStatusList.size
            // 添加 api 获取到的新的帖子
            val indexInSavedList = unsortedTimelineList.indexOfFirst { it.id == items.last().id } + 1
            val statusListAfterIndex =
              unsortedTimelineList.subList(indexInSavedList, unsortedTimelineList.size)
            unsortedTimelineList = (items + statusListAfterIndex).toMutableList()
            uiState = uiState.copy(
              timeline = reorderedStatuses(unsortedTimelineList),
              endReached = items.isEmpty(),
              showNewStatusButton = newStatusCount != 0,
              newStatusCount = newStatusCount.toString()
            )
          }
        }
      } else {
        unsortedTimelineList = items.toMutableList()
        uiState = uiState.copy(
          timeline = reorderedStatuses(unsortedTimelineList),
          endReached = items.isEmpty()
        )
      }
      db.withTransaction {
        // replaceStatusRange(unsortedTimelineList)
        reinsertAllStatus(unsortedTimelineList, activeAccount.id)
      }
    }
  )

  init {
    viewModelScope.launch {
      unsortedTimelineList = timelineDao.getStatuses(activeAccount.id).toMutableList()
      uiState = uiState.copy(timeline = reorderedStatuses(unsortedTimelineList))
      paginator.refresh()
      isInitialLoad = false
      // fetch the latest account info
      api.accountVerifyCredentials(
        domain = activeAccount.domain,
        auth = "Bearer ${activeAccount.accessToken}"
      )
        .fold(
          {
            accountRepository.updateActiveAccount(it)
          },
          {
            it.printStackTrace()
          }
        )
    }
  }

  fun append() = viewModelScope.launch {
    paginator.append()
  }

  fun refreshTimeline() = viewModelScope.launch {
    paginator.refresh()
  }

  fun favoriteStatus(id: String) = viewModelScope.launch {
    api.favouriteStatus(id)
  }

  fun unfavoriteStatus(id: String) = viewModelScope.launch {
    api.unfavouriteStatus(id)
  }

  fun loadUnloadedStatus() {
    val tempList = unsortedTimelineList
    val insertIndex = unsortedTimelineList.indexOfFirst { it.hasUnloadedStatus }
    viewModelScope.launch {
      val response = api.homeTimeline(
        maxId = if (insertIndex != -1) tempList[insertIndex].id else null,
        limit = timelineFetchNumber
      )
      if (response.isSuccessful && !response.body().isNullOrEmpty()) {
        val body = response.body()!!
        val list = body.toMutableList()
        if (tempList.any { it.id == body.last().id }) {
          tempList[insertIndex] = tempList[insertIndex].copy(hasUnloadedStatus = false)
          tempList.addAll(
            index = insertIndex + 1,
            elements = list.filterNot {
              tempList.any { saved -> saved.id == it.id }
            }
          )
          uiState = uiState.copy(timeline = reorderedStatuses(tempList))
        } else {
          list[list.lastIndex] = list[list.lastIndex].copy(hasUnloadedStatus = true)
          when (tempList.indexOfFirst { it.hasUnloadedStatus }) {
            -1 -> tempList.addAll(0, list)
            else -> {
              tempList[insertIndex] = tempList[insertIndex].copy(hasUnloadedStatus = false)
              tempList.addAll(insertIndex + 1, list)
            }
          }
          uiState = uiState.copy(timeline = reorderedStatuses(tempList))
        }
      } else {
        // TODO ERROR
      }
      unsortedTimelineList = tempList
      db.withTransaction {
        // replaceStatusRange(unsortedTimelineList)
        reinsertAllStatus(tempList, activeAccount.id)
      }
    }
  }

  fun dismissButton() {
    uiState = uiState.copy(showNewStatusButton = false)
  }

  private suspend fun reinsertAllStatus(statuses: List<Status>, accountId: Long) {
    timelineDao.clearAll(accountId)
    statuses.forEach { timelineDao.insert(it.toEntity(accountId)) }
  }

  private suspend fun replaceStatusRange(statuses: List<Status>) {
    if (statuses.isNotEmpty()) {
      timelineDao.deleteRange(activeAccount.id, statuses.last().id, statuses.first().id)
    }
    for (status in statuses) {
      timelineDao.insert(status.toEntity(activeAccount.id))
    }
  }
}

data class HomeUiState(
  val timeline: List<Status> = emptyList(),
  val newStatusCount: String = "",
  val showNewStatusButton: Boolean = false,
  val endReached: Boolean = false,
  val timelineLoadState: LoadState = LoadState.NotLoading
)
