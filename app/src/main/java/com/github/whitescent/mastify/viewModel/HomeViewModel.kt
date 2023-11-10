/*
 * Copyright 2023 WhiteScent
 *
 * This file is a part of Mastify.
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * Mastify is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Mastify; if not,
 * see <http://www.gnu.org/licenses>.
 */

package com.github.whitescent.mastify.viewModel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.github.whitescent.mastify.data.repository.AccountRepository
import com.github.whitescent.mastify.data.repository.HomeRepository
import com.github.whitescent.mastify.data.repository.HomeRepository.Companion.FETCHNUMBER
import com.github.whitescent.mastify.database.AppDatabase
import com.github.whitescent.mastify.domain.StatusActionHandler
import com.github.whitescent.mastify.mapper.status.toEntity
import com.github.whitescent.mastify.mapper.status.toUiData
import com.github.whitescent.mastify.network.MastodonApi
import com.github.whitescent.mastify.network.model.status.Status
import com.github.whitescent.mastify.paging.LoadState
import com.github.whitescent.mastify.paging.Paginator
import com.github.whitescent.mastify.utils.StatusAction
import com.github.whitescent.mastify.utils.reorderStatuses
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
  private val db: AppDatabase,
  private val api: MastodonApi,
  private val statusActionHandler: StatusActionHandler,
  private val accountRepository: AccountRepository,
  private val homeRepository: HomeRepository,
) : ViewModel() {

  private val timelineDao = db.timelineDao()
  private var initialKey: String? = null
  private var isInitialLoad = false

  private var timelineFlow = MutableStateFlow<List<Status>>(listOf())
  val timelineList = timelineFlow
    .map { splitReorderStatus(it).toUiData() }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.Eagerly,
      initialValue = listOf()
    )

  val snackBarFlow = statusActionHandler.snackBarFlow

  val activeAccount get() = accountRepository.activeAccount!!

  val timelineScrollPosition get() = activeAccount.firstVisibleItemIndex
  val timelineScrollPositionOffset get() = activeAccount.offset
  var uiState by mutableStateOf(HomeUiState())
    private set

  private val paginator = Paginator(
    initialKey = initialKey,
    refreshKey = null,
    onLoadUpdated = { uiState = uiState.copy(timelineLoadState = it) },
    onRequest = { nextPage ->
      val response = api.homeTimeline(maxId = nextPage, limit = FETCHNUMBER)
      if (response.isSuccessful && !response.body().isNullOrEmpty()) {
        val body = response.body()!!
        Result.success(body)
      } else {
        Result.success(emptyList())
      }
    },
    getNextKey = { items, loadState ->
      // if timelineEntity is not empty, we need set nextPage to the last status id
      if (loadState == LoadState.Append) {
        if (!isInitialLoad && timelineFlow.value.isNotEmpty()) timelineFlow.value.last().id
        else items.lastOrNull()?.id
      } else {
        if (timelineFlow.value.isNotEmpty()) timelineFlow.value.last().id
        else items.lastOrNull()?.id
      }
    },
    onError = {
      it?.printStackTrace()
    },
    onAppend = { items ->
      timelineFlow.emit(timelineFlow.value + items)
      uiState = uiState.copy(endReached = items.isEmpty())
      db.withTransaction {
        timelineDao.insertAll(items.toEntity(activeAccount.id))
      }
    },
    onRefresh = { items ->
      val newStatusCount = items.filterNot {
        timelineFlow.value.any { saved -> saved.id == it.id }
      }.size
      uiState = uiState.copy(
        showNewStatusButton = newStatusCount != 0 && timelineFlow.value.isNotEmpty(),
        newStatusCount = newStatusCount,
        needSecondLoad = !timelineFlow.value.any { it.id == items.last().id },
        endReached = items.size < FETCHNUMBER
      )
      timelineFlow.emit(homeRepository.timelineListHandler(timelineFlow.value, items))
      reinsertAllStatus(timelineFlow.value, activeAccount.id)
    },
  )

  init {
    viewModelScope.launch {
      timelineFlow.emit(timelineDao.getStatuses(activeAccount.id))
      paginator.refresh()
      isInitialLoad = true
      // fetch the latest account info
      homeRepository.updateAccountInfo()
    }
  }

  fun append() = viewModelScope.launch { paginator.append() }

  fun refreshTimeline() = viewModelScope.launch { paginator.refresh() }

  fun onStatusAction(action: StatusAction, context: Context, status: Status) {
    viewModelScope.launch(Dispatchers.IO) {
      // update newStatus to timelineFlow
      timelineFlow.update {
        it.toMutableList().also { list ->
          val index = list.indexOfFirst { saved -> saved.actionableId == status.id }
          list[index] = list[index].copy(
            favorited = if (action is StatusAction.Favorite) action.favorite else status.favorited,
            favouritesCount = if (action is StatusAction.Favorite) {
              if (action.favorite) status.favouritesCount + 1 else status.favouritesCount - 1
            } else status.favouritesCount,
            reblogged = if (action is StatusAction.Reblog) action.reblog else status.reblogged,
            reblogsCount = if (action is StatusAction.Reblog) {
              if (action.reblog) status.reblogsCount + 1 else status.reblogsCount - 1
            } else status.reblogsCount,
          )
        }
      }
      statusActionHandler.onStatusAction(action, context)
      reinsertAllStatus(timelineFlow.value, activeAccount.id)
    }
  }

  fun dismissButton() {
    uiState = uiState.copy(showNewStatusButton = false)
  }

  fun loadUnloadedStatus(statusId: String) {
    val tempList = timelineFlow.value.toMutableList()
    val insertIndex = tempList.indexOfFirst { it.id == statusId }
    viewModelScope.launch {
      val response = api.homeTimeline(
        maxId = tempList.find { it.id == statusId }!!.id,
        limit = FETCHNUMBER
      )
      if (response.isSuccessful && !response.body().isNullOrEmpty()) {
        val list = response.body()!!.toMutableList()
        if (tempList.any { it.id == list.last().id }) {
          tempList[insertIndex] = tempList[insertIndex].copy(hasUnloadedStatus = false)
          tempList.addAll(
            index = insertIndex + 1,
            elements = list.filterNot {
              tempList.any { saved -> saved.id == it.id }
            }
          )
        } else {
          list[list.lastIndex] = list[list.lastIndex].copy(hasUnloadedStatus = true)
          tempList[insertIndex] = tempList[insertIndex].copy(hasUnloadedStatus = false)
          tempList.addAll(insertIndex + 1, list)
        }
        timelineFlow.emit(tempList)
        db.withTransaction {
          reinsertAllStatus(timelineFlow.value, activeAccount.id)
        }
      } else {
        // TODO error handling
      }
    }
  }

  fun updateTimelinePosition(firstVisibleItemIndex: Int, offset: Int) {
    accountRepository.updateActiveAccount(
      activeAccount.copy(firstVisibleItemIndex = firstVisibleItemIndex, offset = offset)
    )
  }

  private suspend fun reinsertAllStatus(statuses: List<Status>, accountId: Long) {
    db.withTransaction {
      timelineDao.clearAll(accountId)
      timelineDao.insertAll(statuses.toEntity(accountId))
    }
  }

  private fun splitReorderStatus(statuses: List<Status>): List<Status> {
    if (statuses.size <= FETCHNUMBER) return reorderStatuses(statuses)
    val result = mutableListOf<Status>()
    val prefix = reorderStatuses(statuses.subList(0, FETCHNUMBER))
    val suffix = reorderStatuses(statuses.subList(FETCHNUMBER, statuses.size))
    result.addAll(prefix + suffix)
    return result
  }
}

data class HomeUiState(
  val newStatusCount: Int = 0,
  val needSecondLoad: Boolean = false,
  val showNewStatusButton: Boolean = false,
  val endReached: Boolean = false,
  val timelineLoadState: LoadState = LoadState.NotLoading
)
