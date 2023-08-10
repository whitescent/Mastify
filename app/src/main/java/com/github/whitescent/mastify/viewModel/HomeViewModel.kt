package com.github.whitescent.mastify.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.github.whitescent.mastify.data.repository.AccountRepository
import com.github.whitescent.mastify.data.repository.HomeRepository
import com.github.whitescent.mastify.database.AppDatabase
import com.github.whitescent.mastify.mapper.status.toEntity
import com.github.whitescent.mastify.mapper.status.toUiData
import com.github.whitescent.mastify.network.MastodonApi
import com.github.whitescent.mastify.network.model.status.Status
import com.github.whitescent.mastify.paging.LoadState
import com.github.whitescent.mastify.paging.Paginator
import com.github.whitescent.mastify.utils.reorderStatuses
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
  private val db: AppDatabase,
  private val accountRepository: AccountRepository,
  private val homeRepository: HomeRepository,
  private val api: MastodonApi,
) : ViewModel() {

  private val timelineDao = db.timelineDao()
  private val timelineFetchNumber = 30
  private var initialKey: String? = null

  private var timelineFlow = MutableStateFlow<List<Status>>(listOf())
  val timelineList = timelineFlow.map { reorderStatuses(it).toUiData() }

  val activeAccount get() = accountRepository.activeAccount!!
  var uiState by mutableStateOf(HomeUiState())
    private set

  private val paginator = Paginator(
    initialKey = initialKey,
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
    getNextKey = { items, loadState ->
      // if timelineEntity is not empty, we need set nextPage to the last status id
      if (loadState == LoadState.Refresh && timelineFlow.value.isNotEmpty())
        timelineFlow.value.last().id
      else items.last().id
    },
    onError = {
      it?.printStackTrace()
    },
    onAppend = { items, newKey ->
      timelineFlow.emit(timelineFlow.value + items)
      uiState = uiState.copy(endReached = items.isEmpty())
      initialKey = newKey
      db.withTransaction {
        timelineDao.insertAll(items.toEntity(activeAccount.id))
      }
    },
    onRefresh = { items ->
      if (timelineFlow.value.isNotEmpty()) {
        val lastStatusInApi = items.last()
        if (timelineFlow.value.any { it.id == lastStatusInApi.id }) {
          val newStatusList = items.filterNot {
            timelineFlow.value.any { saved -> saved.id == it.id }
          }
          val newStatusCount = newStatusList.size
          // Add / Update / Remove posts obtained by api
          val indexInSavedList = timelineFlow.value.indexOfFirst { it.id == items.last().id } + 1
          val statusListAfterIndex =
            timelineFlow.value.subList(indexInSavedList, timelineFlow.value.size)
          timelineFlow.emit(items + statusListAfterIndex)
          uiState = uiState.copy(
            endReached = items.isEmpty(),
            showNewStatusButton = newStatusCount != 0,
            newStatusCount = newStatusCount.toString()
          )
          reinsertAllStatus(timelineFlow.value, activeAccount.id)
        }
      } else {
        timelineFlow.emit(items)
        uiState = uiState.copy(endReached = items.isEmpty())
        timelineDao.insertAll(items.map { it.toEntity(activeAccount.id) })
      }
    }
  )

  init {
    viewModelScope.launch {
      timelineFlow.emit(timelineDao.getStatuses(activeAccount.id))
      paginator.refresh()
      // fetch the latest account info
      homeRepository.updateAccountInfo()
    }
  }

  fun append() = viewModelScope.launch { paginator.append() }

  fun refreshTimeline() = viewModelScope.launch { paginator.refresh() }

  fun favoriteStatus(id: String) = viewModelScope.launch { api.favouriteStatus(id) }

  fun unfavoriteStatus(id: String) = viewModelScope.launch { api.unfavouriteStatus(id) }

  fun dismissButton() {
    uiState = uiState.copy(showNewStatusButton = false)
  }

  private suspend fun reinsertAllStatus(statuses: List<Status>, accountId: Long) {
    db.withTransaction {
      timelineDao.clearAll(accountId)
      timelineDao.insertAll(statuses.toEntity(accountId))
    }
  }
}

data class HomeUiState(
  val newStatusCount: String = "",
  val showNewStatusButton: Boolean = false,
  val endReached: Boolean = false,
  val timelineLoadState: LoadState = LoadState.NotLoading
)
