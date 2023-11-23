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
import com.github.whitescent.mastify.network.MastodonApi
import com.github.whitescent.mastify.network.model.status.Status
import com.github.whitescent.mastify.paging.LoadState
import com.github.whitescent.mastify.paging.Paginator
import com.github.whitescent.mastify.utils.StatusAction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
  private val db: AppDatabase,
  private val api: MastodonApi,
  private val statusActionHandler: StatusActionHandler,
  private val accountRepository: AccountRepository,
  private val homeRepository: HomeRepository,
) : ViewModel() {

  private val timelineDao = db.timelineDao()
  private val accountDao = db.accountDao()

  private val activeAccountFlow = accountDao.getActiveAccountFlow().filterNotNull()
  private val timelineWithAccountFlow = activeAccountFlow
    .flatMapLatest {
      timelineDao.getStatusListWithFlow(it.id)
    }

  private var timelineMemoryFlow = MutableStateFlow<List<Status>>(emptyList())

  val currentAccountAvatar = activeAccountFlow
    .map { it.profilePictureUrl }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.Eagerly,
      initialValue = ""
    )

  private val paginator = Paginator(
    getAppendKey = {
      timelineMemoryFlow.value.lastOrNull()?.id
    },
    onLoadUpdated = {
      uiState = uiState.copy(timelineLoadState = it)
    },
    refreshKey = null,
    onRequest = { page ->
      val response = api.homeTimeline(maxId = page, limit = FETCHNUMBER)
      if (response.isSuccessful && !response.body().isNullOrEmpty()) {
        val body = response.body()!!
        Result.success(body)
      } else {
        Result.success(emptyList())
      }
    },
    onError = {
      it?.printStackTrace()
    },
    onSuccess = { loadState, items ->
      when (loadState) {
        LoadState.Append -> {
          uiState = uiState.copy(endReached = items.isEmpty())
          db.withTransaction {
            val activeAccount = accountDao.getActiveAccount()!!
            timelineDao.insertAll(items.toEntity(activeAccount.id))
          }
        }
        LoadState.Refresh -> {
          val newStatusCount = items.filterNot {
            timelineMemoryFlow.value.any { saved -> saved.id == it.id }
          }.size
          uiState = uiState.copy(
            showNewStatusButton = newStatusCount != 0 && timelineMemoryFlow.value.isNotEmpty(),
            newStatusCount = newStatusCount,
            needSecondLoad = !timelineMemoryFlow.value.any { it.id == items.last().id },
            endReached = items.size < FETCHNUMBER
          )
          timelineMemoryFlow.value =
            homeRepository.updateTimelineOnRefresh(timelineMemoryFlow.value, items)
          reinsertAllStatus(timelineMemoryFlow.value)
        }
        else -> Unit
      }
    }
  )

  init {
    viewModelScope.launch {
      launch { // sync memory list
        timelineWithAccountFlow.collect {
          timelineMemoryFlow.emit(it)
        }
      }
      // fetchLatestAccountData()
      // refreshTimeline()
    }
  }

  val snackBarFlow = statusActionHandler.snackBarFlow

  var uiState by mutableStateOf(HomeUiState())
    private set

  fun append() = viewModelScope.launch { paginator.append() }

  fun refreshTimeline() = viewModelScope.launch { paginator.refresh() }

  fun fetchLatestAccountData() = viewModelScope.launch { homeRepository.updateAccountInfo() }

  fun onStatusAction(action: StatusAction, context: Context, actionableStatus: Status) {
    viewModelScope.launch(Dispatchers.IO) {
      val activeAccount = accountDao.getActiveAccount()!!
      // update the currentStatus in the db, if the action is about currentStatus
      var savedStatus = timelineMemoryFlow.value.firstOrNull {
        it.actionableId == actionableStatus.id
      }
      savedStatus?.let {
        // if this status include reblog's status, we should update reblog's status
        when (it.reblog == null) {
          true -> {
            savedStatus = it.copy(
              favorited = if (action is StatusAction.Favorite) action.favorite else actionableStatus.favorited,
              favouritesCount = if (action is StatusAction.Favorite) {
                if (action.favorite) actionableStatus.favouritesCount + 1 else actionableStatus.favouritesCount - 1
              } else actionableStatus.favouritesCount,
              reblogged = if (action is StatusAction.Reblog) action.reblog else actionableStatus.reblogged,
              reblogsCount = if (action is StatusAction.Reblog) {
                if (action.reblog) actionableStatus.reblogsCount + 1 else actionableStatus.reblogsCount - 1
              } else actionableStatus.reblogsCount,
              bookmarked = if (action is StatusAction.Bookmark) action.bookmark else actionableStatus.bookmarked
            )
          }
          else -> {
            savedStatus = savedStatus!!.copy(
              reblog = it.reblog.copy(
                favorited = if (action is StatusAction.Favorite) action.favorite else actionableStatus.favorited,
                favouritesCount = if (action is StatusAction.Favorite) {
                  if (action.favorite) actionableStatus.favouritesCount + 1 else actionableStatus.favouritesCount - 1
                } else actionableStatus.favouritesCount,
                reblogged = if (action is StatusAction.Reblog) action.reblog else actionableStatus.reblogged,
                reblogsCount = if (action is StatusAction.Reblog) {
                  if (action.reblog) actionableStatus.reblogsCount + 1 else actionableStatus.reblogsCount - 1
                } else actionableStatus.reblogsCount,
                bookmarked = if (action is StatusAction.Bookmark) action.bookmark else actionableStatus.bookmarked
              )
            )
          }
        }
        timelineDao.insertOrUpdate(savedStatus!!.toEntity(activeAccount.id))
      }
      statusActionHandler.onStatusAction(action, context)
    }
  }

  fun dismissButton() {
    uiState = uiState.copy(showNewStatusButton = false)
  }

  /*
  * Load those APIs that don't get the current database of the latest posts at once
  * e.g: database: Status ID 1100 - 1000, Latest Timeline: 1500
  * The API can only get 40 at a time, so the user needs to manually get the middle part of the post
  */
  fun loadUnloadedStatus(statusId: String) {
    val tempList = timelineMemoryFlow.value.toMutableList()
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
        timelineMemoryFlow.value = tempList
        reinsertAllStatus(timelineMemoryFlow.value)
      } else {
        // TODO error handling
      }
    }
  }

  suspend fun updateTimelinePosition(firstVisibleItemIndex: Int, offset: Int) {
    val activeAccount = accountDao.getActiveAccount()!!
    db.withTransaction {
      accountRepository.updateActiveAccount(
        activeAccount.copy(firstVisibleItemIndex = firstVisibleItemIndex, offset = offset)
      )
    }
  }

  private suspend fun reinsertAllStatus(statuses: List<Status>) {
    db.withTransaction {
      val accountId = accountDao.getActiveAccount()!!.id
      timelineDao.clearAll(accountId)
      timelineDao.insertAll(statuses.toEntity(accountId))
    }
  }
}

data class HomeUiState(
  val newStatusCount: Int = 0,
  val needSecondLoad: Boolean = false,
  val showNewStatusButton: Boolean = false,
  val endReached: Boolean = false,
  val timelineLoadState: LoadState = LoadState.NotLoading
)
