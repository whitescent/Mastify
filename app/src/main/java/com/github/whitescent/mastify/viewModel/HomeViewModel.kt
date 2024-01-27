/*
 * Copyright 2024 WhiteScent
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
import com.github.whitescent.mastify.data.model.ui.StatusUiData
import com.github.whitescent.mastify.data.repository.HomeRepository
import com.github.whitescent.mastify.data.repository.HomeRepository.Companion.FETCHNUMBER
import com.github.whitescent.mastify.database.AppDatabase
import com.github.whitescent.mastify.database.model.AccountEntity
import com.github.whitescent.mastify.domain.StatusActionHandler
import com.github.whitescent.mastify.domain.StatusActionHandler.Companion.updatePollOfStatus
import com.github.whitescent.mastify.domain.StatusActionHandler.Companion.updateSingleStatusActions
import com.github.whitescent.mastify.mapper.toEntity
import com.github.whitescent.mastify.mapper.toUiData
import com.github.whitescent.mastify.network.model.status.Status
import com.github.whitescent.mastify.paging.LoadState
import com.github.whitescent.mastify.paging.Paginator
import com.github.whitescent.mastify.utils.PostState
import com.github.whitescent.mastify.utils.StatusAction
import com.github.whitescent.mastify.utils.StatusAction.VotePoll
import com.github.whitescent.mastify.utils.splitReorderStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
  db: AppDatabase,
  private val statusActionHandler: StatusActionHandler,
  private val homeRepository: HomeRepository,
) : ViewModel() {

  private val timelineDao = db.timelineDao()
  private val accountDao = db.accountDao()

  private val activeAccountFlow = accountDao.getActiveAccountFlow().filterNotNull()
  private val timelineWithAccountFlow = activeAccountFlow
    .flatMapLatest {
      timelineDao.getStatusListWithFlow(it.id)
    }

  // The latest untransformed Timeline data from the database
  private var timelineMemoryFlow = MutableStateFlow<List<Status>>(emptyList())

  val homeCombinedFlow = activeAccountFlow
    .flatMapLatest { account ->
      val timelineFlow = timelineDao.getStatusListWithFlow(account.id)
      timelineFlow.map {
        HomeUserData(
          activeAccount = account,
          timeline = splitReorderStatus(it).toUiData().toImmutableList(),
          position = TimelinePosition(account.firstVisibleItemIndex, account.offset)
        )
      }
    }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.Eagerly,
      initialValue = null
    )

  val paginator = Paginator(
    getAppendKey = {
      timelineMemoryFlow.value.lastOrNull()?.id
    },
    onLoadUpdated = {
      uiState = uiState.copy(timelineLoadState = it)
    },
    refreshKey = null,
    onRequest = { page ->
      homeRepository.fetchTimeline(maxId = page)
    },
    onError = {
      it?.printStackTrace()
    },
    onSuccess = { loadState, items ->
      when (loadState) {
        LoadState.Append -> {
          uiState = uiState.copy(endReached = items.isEmpty())
          homeRepository.appendTimelineFromApi(items)
          // We need to wait for db to emit the latest List before we can end onSuccess,
          // otherwise loadState will be equal to NotLoading in advance,
          // and the List has not been updated, which will cause LazyColumn to jitter
          delay(200)
        }
        LoadState.Refresh -> {
          val newStatusCount = items.filterNot {
            timelineMemoryFlow.value.any { saved -> saved.id == it.id }
          }.size
          uiState = uiState.copy(
            toastButton = HomeNewStatusToastModel(
              showNewToastButton = newStatusCount != 0 && timelineMemoryFlow.value.isNotEmpty(),
              newStatusCount = newStatusCount,
              showManyPost = !timelineMemoryFlow.value.any { it.id == items.last().id },
            ),
            endReached = items.size < FETCHNUMBER
          )
          homeRepository.refreshTimelineFromApi(timelineMemoryFlow.value, items)
        }
        else -> Unit
      }
    }
  )

  init {
    viewModelScope.launch {
      // sync memory list
      timelineWithAccountFlow.collect {
        timelineMemoryFlow.emit(it)
      }
    }
  }

  val snackBarFlow = statusActionHandler.snackBarFlow

  var uiState by mutableStateOf(HomeUiState())
    private set

  var loadMoreState by mutableStateOf<PostState>(PostState.Idle)
    private set

  fun append() = viewModelScope.launch {
    paginator.append()
  }

  fun refreshTimeline() = viewModelScope.launch {
    paginator.refresh()
  }

  fun onStatusAction(action: StatusAction, context: Context, actionableStatus: Status) {
    viewModelScope.launch(Dispatchers.IO) {
      val activeAccount = accountDao.getActiveAccount()!!
      // update the current Status in the db, if the action is about (fav, reblog, bookmark) etc...
      var savedStatus = timelineMemoryFlow.value.firstOrNull {
        it.actionableId == actionableStatus.id
      }
      // The reason for this separation is that we need to update the UI state as quickly as possible,
      // regardless of whether the network request was successful or not.
      if (savedStatus != null) {
        if (action !is VotePoll) {
          savedStatus = updateSingleStatusActions(savedStatus, action)
          timelineDao.insertOrUpdate(savedStatus.toEntity(activeAccount.id))
          statusActionHandler.onStatusAction(action, context)
        } else {
          statusActionHandler.onStatusAction(action, context)?.let {
            if (it.isSuccess) {
              savedStatus = updatePollOfStatus(savedStatus!!, it.getOrNull()!!.poll!!)
              timelineDao.insertOrUpdate(savedStatus!!.toEntity(activeAccount.id))
            }
          }
        }
      }
    }
  }

  fun dismissButton() {
    uiState = uiState.copy(
      toastButton = uiState.toastButton.copy(
        showNewToastButton = false
      )
    )
  }

  /**
   * Load those APIs that don't get the current database of the latest posts at once
   * e.g: database: Status ID 1100 - 1000 (Sort descending, larger numbers indicate newer posts)
   * Latest Timeline: 1500
   * The API can only get 40 at a time, so the user needs to manually get the middle part of the post
   */
  fun loadUnloadedStatus(statusId: String) {
    viewModelScope.launch {
      loadMoreState = PostState.Posting
      homeRepository.fillMissingStatusesAround(statusId, timelineMemoryFlow.value)
      loadMoreState = PostState.Idle
    }
  }

  /**
   * Stores the user's current browsing location
   */
  suspend fun updateTimelinePosition(firstVisibleItemIndex: Int, offset: Int) =
    homeRepository.storeLastViewedTimelineOffset(firstVisibleItemIndex, offset)

  private suspend fun refreshTimelineWithLatestStatuses(statuses: List<Status>) =
    homeRepository.saveLatestTimelineToDb(statuses)
}

data class HomeUserData(
  val activeAccount: AccountEntity,
  val timeline: ImmutableList<StatusUiData>,
  val position: TimelinePosition
)

data class HomeNewStatusToastModel(
  val showNewToastButton: Boolean = false,
  val newStatusCount: Int = 0,
  val showManyPost: Boolean = false,
)

data class HomeUiState(
  val toastButton: HomeNewStatusToastModel = HomeNewStatusToastModel(),
  val endReached: Boolean = false,
  val timelineLoadState: LoadState = LoadState.NotLoading
)
