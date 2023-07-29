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
import com.github.whitescent.mastify.mapper.status.toEntity
import com.github.whitescent.mastify.network.MastodonApi
import com.github.whitescent.mastify.network.model.status.Status
import com.github.whitescent.mastify.paging.LoadState
import com.github.whitescent.mastify.paging.Paginator
import com.github.whitescent.mastify.utils.reorderedStatuses
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
  private val db: AppDatabase,
  private val accountRepository: AccountRepository,
  private val api: MastodonApi,
) : ViewModel() {

  private val timelineDao = db.timelineDao()
  private val timelineFetchNumber = 30
  private var nextPage: String? = null
  private var isInitialLoad = true
  private var timelineListFlow = MutableStateFlow<List<Status>>(listOf())
  val timelineList = timelineListFlow.asStateFlow()

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
      timelineListFlow.emit(timelineListFlow.value + reorderedStatuses(items, api))
      uiState = uiState.copy(endReached = items.isEmpty())
      nextPage = newKey
      db.withTransaction {
        reinsertAllStatus(timelineListFlow.value, activeAccount.id)
      }
    },
    onRefresh = { items ->
      if (timelineListFlow.value.isNotEmpty()) {
        val lastStatusInApi = items.last()
        when {
          !timelineListFlow.value.any { it.id == lastStatusInApi.id } && isInitialLoad -> {
            uiState = uiState.copy(
              showNewStatusButton = true,
              newStatusCount = "$timelineFetchNumber+"
            )
            loadUnloadedStatus()
          }
          timelineListFlow.value.any { it.id == lastStatusInApi.id } &&
            !timelineListFlow.value.any { it.hasUnloadedStatus } -> {
            val newStatusList = items.filterNot {
              timelineListFlow.value.any { saved -> saved.id == it.id }
            }
            val newStatusCount = newStatusList.size
            // 添加 api 获取到的新的帖子
            val indexInSavedList = timelineListFlow.value.indexOfFirst { it.id == items.last().id } + 1
            val statusListAfterIndex =
              timelineListFlow.value.subList(indexInSavedList, timelineListFlow.value.size)
            timelineListFlow.emit((reorderedStatuses(items, api) + statusListAfterIndex))
            uiState = uiState.copy(
              endReached = items.isEmpty(),
              showNewStatusButton = newStatusCount != 0,
              newStatusCount = newStatusCount.toString()
            )
          }
        }
      } else {
        timelineListFlow.emit(reorderedStatuses(items, api))
        uiState = uiState.copy(endReached = items.isEmpty())
      }
      db.withTransaction {
        reinsertAllStatus(timelineListFlow.value, activeAccount.id)
      }
    }
  )

  init {
    viewModelScope.launch {
      timelineListFlow.emit(timelineDao.getAll(activeAccount.id))
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

  fun append() = viewModelScope.launch { paginator.append() }

  fun refreshTimeline() = viewModelScope.launch { paginator.refresh() }

  fun favoriteStatus(id: String) = viewModelScope.launch { api.favouriteStatus(id) }

  fun unfavoriteStatus(id: String) = viewModelScope.launch { api.unfavouriteStatus(id) }

  fun loadUnloadedStatus() {
    var tempList = timelineListFlow.value.toMutableList()
    val insertIndex = timelineListFlow.value.indexOfFirst { it.hasUnloadedStatus }
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
          tempList = reorderedStatuses(tempList, api).toMutableList()
          timelineListFlow.update { tempList }
        } else {
          list[list.lastIndex] = list[list.lastIndex].copy(hasUnloadedStatus = true)
          when (tempList.indexOfFirst { it.hasUnloadedStatus }) {
            -1 -> tempList.addAll(0, list)
            else -> {
              tempList[insertIndex] = tempList[insertIndex].copy(hasUnloadedStatus = false)
              tempList.addAll(insertIndex + 1, list)
            }
          }
          tempList = reorderedStatuses(tempList, api).toMutableList()
          timelineListFlow.update { tempList }
        }
      } else {
        // TODO ERROR
      }
      db.withTransaction {
        reinsertAllStatus(timelineListFlow.value, activeAccount.id)
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
}

data class HomeUiState(
  val newStatusCount: String = "",
  val showNewStatusButton: Boolean = false,
  val endReached: Boolean = false,
  val timelineLoadState: LoadState = LoadState.NotLoading
)
