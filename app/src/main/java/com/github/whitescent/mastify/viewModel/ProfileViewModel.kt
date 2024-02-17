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

import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.whitescent.R
import com.github.whitescent.mastify.data.model.StatusBackResult
import com.github.whitescent.mastify.data.repository.AccountRepository
import com.github.whitescent.mastify.data.repository.StatusRepository
import com.github.whitescent.mastify.database.AppDatabase
import com.github.whitescent.mastify.extensions.updateStatusActionData
import com.github.whitescent.mastify.mapper.toUiData
import com.github.whitescent.mastify.network.model.account.Account
import com.github.whitescent.mastify.network.model.account.Relationship
import com.github.whitescent.mastify.network.model.status.Status
import com.github.whitescent.mastify.paging.Paginator
import com.github.whitescent.mastify.paging.factory.ProfilePagingFactory
import com.github.whitescent.mastify.screen.navArgs
import com.github.whitescent.mastify.screen.profile.ProfileNavArgs
import com.github.whitescent.mastify.usecase.TimelineUseCase
import com.github.whitescent.mastify.usecase.TimelineUseCase.Companion.updatePollOfStatusList
import com.github.whitescent.mastify.usecase.TimelineUseCase.Companion.updateStatusListActions
import com.github.whitescent.mastify.utils.PostState
import com.github.whitescent.mastify.utils.StatusAction
import com.github.whitescent.mastify.utils.StatusAction.VotePoll
import com.github.whitescent.mastify.viewModel.ProfileKind.StatusWithMedia
import com.github.whitescent.mastify.viewModel.ProfileKind.StatusWithReply
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
  savedStateHandle: SavedStateHandle,
  db: AppDatabase,
  statusRepository: StatusRepository,
  private val timelineUseCase: TimelineUseCase,
  private val accountRepository: AccountRepository,
) : ViewModel() {

  private val navArgs: ProfileNavArgs = savedStateHandle.navArgs()
  private val accountDao = db.accountDao()

  val snackBarFlow = timelineUseCase.snackBarFlow

  private var currentProfileKindFlow = MutableStateFlow(ProfileKind.Status)
  val currentProfileKind = currentProfileKindFlow
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.Eagerly,
      initialValue = ProfileKind.Status
    )

  private val statusPagingFactory =
    ProfilePagingFactory(ProfileKind.Status, navArgs.account.id, statusRepository)

  private val statusWithReplyPagingFactory =
    ProfilePagingFactory(StatusWithReply, navArgs.account.id, statusRepository)

  private val statusWithMediaPagingFactory =
    ProfilePagingFactory(StatusWithMedia, navArgs.account.id, statusRepository)

  val statusPaginator = Paginator(
    pageSize = FETCH_NUMBER,
    pagingFactory = statusPagingFactory
  )

  val statusWithReplyPaginator = Paginator(
    pageSize = FETCH_NUMBER,
    initRefresh = false,
    pagingFactory = statusWithReplyPagingFactory
  )

  val statusWithMediaPaginator = Paginator(
    pageSize = FETCH_NUMBER,
    initRefresh = false,
    pagingFactory = statusWithMediaPagingFactory
  )

  var uiState by mutableStateOf(ProfileUiState(account = navArgs.account))
    private set

  val profileStatus = statusPagingFactory.list
    .map { it.toUiData() }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.Eagerly,
      initialValue = persistentListOf()
    )

  val profileStatusWithReply = statusWithReplyPagingFactory.list
    .map { it.toUiData() }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.Eagerly,
      initialValue = persistentListOf()
    )

  val profileStatusWithMedia = statusWithMediaPagingFactory.list
    .map { it.toUiData() }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.Eagerly,
      initialValue = persistentListOf()
    )

  init {
    viewModelScope.launch {
      uiState = uiState.copy(
        isSelf = navArgs.account.id == accountDao.getActiveAccount()!!.accountId
      )
      combine(
        accountRepository.fetchAccount(navArgs.account.id),
        accountRepository.fetchAccountRelationship(listOf(navArgs.account.id))
      ) { account, relationship -> Pair(account, relationship) }
        .catch { }
        .collect {
          val account = it.first
          val relationships = it.second
          uiState = uiState.copy(
            relationship = relationships.firstOrNull(),
            account = account
          )
        }
      launch {
        currentProfileKindFlow.collect {
          when (it) {
            StatusWithReply ->
              if (profileStatusWithReply.value.isEmpty()) statusWithReplyPaginator.refresh()
            StatusWithMedia ->
              if (profileStatusWithMedia.value.isEmpty()) statusWithMediaPaginator.refresh()
            else -> Unit
          }
        }
      }
    }
  }

  fun followAccount(follow: Boolean, notify: Boolean? = null) {
    viewModelScope.launch {
      uiState = uiState.copy(followState = PostState.Posting)
      accountRepository
        .followAccount(navArgs.account.id, follow, notify)
        .catch {
          uiState = uiState.copy(followState = PostState.Failure(it))
        }
        .collect {
          uiState = uiState.copy(
            relationship = it,
            followState = PostState.Success
          )
        }
    }
  }

  fun lookupAccount(acct: String) {
    viewModelScope.launch {
      uiState = uiState.copy(searchState = PostState.Posting)
      accountRepository.lookupAccount(acct)
        .catch {
          uiState = uiState.copy(searchState = PostState.Failure(it))
        }
        .collect {
          uiState = uiState.copy(
            searchedAccount = it,
            searchState = PostState.Success
          )
          delay(500)
          uiState = uiState.copy(searchState = PostState.Idle)
        }
    }
  }

  fun syncProfileTab(page: Int) {
    currentProfileKindFlow.value = ProfileKind.entries.toTypedArray()[page]
  }

  fun onStatusAction(action: StatusAction, status: Status) {
    viewModelScope.launch(Dispatchers.IO) {
      statusPagingFactory.list.update {
        updateStatusListActions(it, action, status.id)
      }
      statusWithReplyPagingFactory.list.update {
        updateStatusListActions(it, action, status.id)
      }
      statusWithMediaPagingFactory.list.update {
        updateStatusListActions(it, action, status.id)
      }
      timelineUseCase.onStatusAction(action)?.let { response ->
        if (action is VotePoll && response.isSuccess) {
          val targetStatus = response.getOrNull()!!
          statusPagingFactory.list.update {
            updatePollOfStatusList(it, targetStatus.id, targetStatus.poll!!)
          }
          statusWithReplyPagingFactory.list.update {
            updatePollOfStatusList(it, targetStatus.id, targetStatus.poll!!)
          }
        }
      }
    }
  }

  fun updateStatusFromDetailScreen(newStatus: StatusBackResult) {
    val status = statusPagingFactory.list.value
    val statusWithReply = statusWithReplyPagingFactory.list.value
    val statusWithMedia = statusWithMediaPagingFactory.list.value
    statusPagingFactory.list.value = status.updateStatusActionData(newStatus)
    statusWithReplyPagingFactory.list.value = statusWithReply.updateStatusActionData(newStatus)
    statusWithMediaPagingFactory.list.value = statusWithMedia.updateStatusActionData(newStatus)
  }

  companion object {
    const val FETCH_NUMBER = 20
  }
}

data class ProfileUiState(
  val account: Account,
  val searchedAccount: Account? = null,
  val isSelf: Boolean? = null,
  val relationship: Relationship? = null,
  val followState: PostState = PostState.Idle,
  val searchState: PostState = PostState.Idle
)

enum class ProfileKind(
  @StringRes val stringRes: Int
) {
  Status(R.string.post_title),
  StatusWithReply(R.string.reply_title),
  StatusWithMedia(R.string.media_title)
}
