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

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.whitescent.mastify.data.model.ui.StatusUiData
import com.github.whitescent.mastify.data.repository.HomeRepository
import com.github.whitescent.mastify.data.repository.HomeRepository.Companion.FETCH_NUMBER
import com.github.whitescent.mastify.database.AppDatabase
import com.github.whitescent.mastify.database.model.AccountEntity
import com.github.whitescent.mastify.di.ApplicationScope
import com.github.whitescent.mastify.mapper.toEntity
import com.github.whitescent.mastify.mapper.toUiData
import com.github.whitescent.mastify.network.model.status.Status
import com.github.whitescent.mastify.paging.Paginator
import com.github.whitescent.mastify.paging.factory.HomePagingFactory
import com.github.whitescent.mastify.usecase.TimelineUseCase
import com.github.whitescent.mastify.usecase.TimelineUseCase.Companion.updatePollOfStatus
import com.github.whitescent.mastify.usecase.TimelineUseCase.Companion.updateSingleStatusActions
import com.github.whitescent.mastify.utils.PostState
import com.github.whitescent.mastify.utils.PostState.Idle
import com.github.whitescent.mastify.utils.StatusAction
import com.github.whitescent.mastify.utils.StatusAction.VotePoll
import com.github.whitescent.mastify.utils.splitReorderStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
  db: AppDatabase,
  @ApplicationScope private val applicationScope: CoroutineScope,
  private val timelineUseCase: TimelineUseCase,
  private val homeRepository: HomeRepository,
) : ViewModel() {

  private val timelineDao = db.timelineDao()
  private val accountDao = db.accountDao()

  private val activeAccountFlow = accountDao
    .getActiveAccountFlow()
    .filterNotNull()

  private val pagingFactory = HomePagingFactory(db, applicationScope, homeRepository)

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
    pageSize = FETCH_NUMBER,
    pagingFactory = pagingFactory,
    coroutineScope = applicationScope
  )

  init {
    viewModelScope.launch {
      pagingFactory.refreshEventFlow.collect { toastButton ->
        uiState = uiState.copy(toastButton = toastButton)
      }
    }
  }

  val snackBarFlow = timelineUseCase.snackBarFlow

  var uiState by mutableStateOf(HomeUiState())
    private set

  var loadMoreState by mutableStateOf<PostState>(Idle)
    private set

  fun append() = viewModelScope.launch {
    paginator.append()
  }

  fun refreshTimeline() = viewModelScope.launch {
    paginator.refresh()
  }

  fun onStatusAction(action: StatusAction, actionableStatus: Status) {
    viewModelScope.launch(Dispatchers.IO) {
      val activeAccount = accountDao.getActiveAccount()!!
      val timeline = timelineDao.getStatusList(activeAccount.id)
      // update the current Status in the db, if the action is about (fav, reblog, bookmark) etc...
      var savedStatus = timeline.firstOrNull {
        it.actionableId == actionableStatus.id
      }
      // The reason for this separation is that we need to update the UI state as quickly as possible,
      // regardless of whether the network request was successful or not.
      if (savedStatus != null) {
        if (action !is VotePoll) {
          savedStatus = updateSingleStatusActions(savedStatus, action)
          timelineDao.insertOrUpdate(savedStatus.toEntity(activeAccount.id))
          timelineUseCase.onStatusAction(action)
        } else {
          timelineUseCase.onStatusAction(action)?.let {
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
      val activeAccount = accountDao.getActiveAccount()!!
      val timeline = timelineDao.getStatusList(activeAccount.id)
      homeRepository.fillMissingStatusesAround(statusId, timeline)
      loadMoreState = Idle
    }
  }

  /**
   * Stores the user's current browsing location
   */
  suspend fun updateTimelinePosition(firstVisibleItemIndex: Int, offset: Int) =
    homeRepository.saveLastViewedTimelineOffset(firstVisibleItemIndex, offset)
}

data class TimelinePosition(
  val index: Int = 0,
  val offset: Int = 0
)

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
  val toastButton: HomeNewStatusToastModel = HomeNewStatusToastModel()
)
