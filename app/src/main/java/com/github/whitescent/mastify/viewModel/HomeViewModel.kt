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
import com.github.whitescent.mastify.mapper.status.toUiData
import com.github.whitescent.mastify.network.MastodonApi
import com.github.whitescent.mastify.network.model.status.Status
import com.github.whitescent.mastify.paging.LoadState
import com.github.whitescent.mastify.paging.Paginator
import com.github.whitescent.mastify.utils.reorderedStatuses
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
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

  private var timelineFlow = MutableStateFlow<List<Status>>(listOf())
  val timelineList = timelineFlow.asStateFlow().map { reorderedStatuses(it).toUiData() }

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
      timelineFlow.emit(timelineFlow.value + items)
      uiState = uiState.copy(endReached = items.isEmpty())
      nextPage = newKey
      db.withTransaction {
        timelineDao.insertAll(timelineFlow.value.map { it.toEntity(activeAccount.id) })
      }
    },
    onRefresh = { items ->
      if (timelineFlow.value.isNotEmpty()) {
        val lastStatusInApi = items.last()
        when {
          !timelineFlow.value.any { it.id == lastStatusInApi.id } && isInitialLoad -> {
            uiState = uiState.copy(
              showNewStatusButton = true,
              newStatusCount = "$timelineFetchNumber+"
            )
            loadUnloadedStatus()
          }
          timelineFlow.value.any { it.id == lastStatusInApi.id } &&
            !timelineFlow.value.any { it.hasUnloadedStatus } -> {
            val newStatusList = items.filterNot {
              timelineFlow.value.any { saved -> saved.id == it.id }
            }
            val newStatusCount = newStatusList.size
            // 添加 api 获取到的新帖子
            val indexInSavedList = timelineFlow.value.indexOfFirst { it.id == items.last().id } + 1
            val statusListAfterIndex =
              timelineFlow.value.subList(indexInSavedList, timelineFlow.value.size)
            timelineFlow.emit(items + statusListAfterIndex)
            uiState = uiState.copy(
              endReached = items.isEmpty(),
              showNewStatusButton = newStatusCount != 0,
              newStatusCount = newStatusCount.toString()
            )
            db.withTransaction {
              reinsertAllStatus()
            }
          }
        }
      } else {
        timelineFlow.emit(items)
        uiState = uiState.copy(endReached = items.isEmpty())
        db.withTransaction {
          reinsertAllStatus()
        }
      }
    }
  )

  init {
    viewModelScope.launch {
      timelineFlow.emit(timelineDao.getStatuses(activeAccount.id))
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
    val tempList = timelineFlow.value.toMutableList()
    val insertIndex = timelineFlow.value.indexOfFirst { it.hasUnloadedStatus }
    viewModelScope.launch {
      val response = api.homeTimeline(
        maxId = if (insertIndex != -1) tempList[insertIndex].id else null,
        limit = timelineFetchNumber
      )
      if (response.isSuccessful && !response.body().isNullOrEmpty()) {
        val body = response.body()!!
        val list = body.toMutableList()
        if (tempList.any { it.id == body.last().id }) {
          // 如果 api 获取到的最后一条帖子已经存在于本地数据库中
          // 则取消显示加载更多的按钮，并且完成拼接
          tempList[insertIndex] = tempList[insertIndex].copy(hasUnloadedStatus = false)
          tempList.addAll(
            index = insertIndex + 1,
            elements = list.filterNot {
              tempList.any { saved -> saved.id == it.id }
            }
          )
          timelineFlow.emit(tempList)
        } else {
          list[list.lastIndex] = list[list.lastIndex].copy(hasUnloadedStatus = true)
          when (tempList.indexOfFirst { it.hasUnloadedStatus }) {
            -1 -> tempList.addAll(0, list)
            else -> {
              tempList[insertIndex] = tempList[insertIndex].copy(hasUnloadedStatus = false)
              tempList.addAll(insertIndex + 1, list)
            }
          }
          timelineFlow.emit(tempList)
        }
      } else {
        // TODO ERROR
      }
      db.withTransaction {
        reinsertAllStatus()
      }
    }
  }

  fun dismissButton() {
    uiState = uiState.copy(showNewStatusButton = false)
  }

  private fun reinsertAllStatus() {
    viewModelScope.launch(Dispatchers.IO) {
      db.withTransaction {
        timelineDao.clearAll(activeAccount.id)
        timelineDao.insertAll(timelineFlow.value.map { it.toEntity(activeAccount.id) })
      }
    }
  }
}

data class HomeUiState(
  val newStatusCount: String = "",
  val showNewStatusButton: Boolean = false,
  val endReached: Boolean = false,
  val timelineLoadState: LoadState = LoadState.NotLoading
)
