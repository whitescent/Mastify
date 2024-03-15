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

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.connyduck.calladapter.networkresult.onFailure
import at.connyduck.calladapter.networkresult.onSuccess
import com.github.whitescent.mastify.data.model.StatusBackResult
import com.github.whitescent.mastify.data.model.ui.StatusUiData
import com.github.whitescent.mastify.data.repository.InstanceRepository
import com.github.whitescent.mastify.data.repository.StatusRepository
import com.github.whitescent.mastify.database.AppDatabase
import com.github.whitescent.mastify.extensions.updateStatusActionData
import com.github.whitescent.mastify.mapper.toEntity
import com.github.whitescent.mastify.mapper.toUiData
import com.github.whitescent.mastify.network.model.emoji.Emoji
import com.github.whitescent.mastify.network.model.status.Status
import com.github.whitescent.mastify.screen.navArgs
import com.github.whitescent.mastify.screen.other.StatusDetailNavArgs
import com.github.whitescent.mastify.ui.component.dialog.ReplyThread
import com.github.whitescent.mastify.ui.component.generateHtmlContentWithEmoji
import com.github.whitescent.mastify.usecase.TimelineUseCase
import com.github.whitescent.mastify.usecase.TimelineUseCase.Companion.updatePollOfStatusList
import com.github.whitescent.mastify.usecase.TimelineUseCase.Companion.updateStatusListActions
import com.github.whitescent.mastify.utils.PostState
import com.github.whitescent.mastify.utils.StatusAction
import com.github.whitescent.mastify.utils.StatusAction.VotePoll
import com.github.whitescent.mastify.utils.onFailure
import com.github.whitescent.mastify.utils.onSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalFoundationApi::class)
class StatusDetailViewModel @Inject constructor(
  savedStateHandle: SavedStateHandle,
  db: AppDatabase,
  private val timelineUseCase: TimelineUseCase,
  private val statusRepository: StatusRepository,
  private val instanceRepository: InstanceRepository
) : ViewModel() {

  private val timelineDao = db.timelineDao()
  private val accountDao = db.accountDao()

  val snackBarFlow = timelineUseCase.snackBarFlow
  val navArgs: StatusDetailNavArgs = savedStateHandle.navArgs()

  val replyField by mutableStateOf(TextFieldState())

  var uiState by mutableStateOf(StatusDetailUiState())
    private set

  /**
   * Navigate from the previous screen to the status with the latest data
   * in the Status Detail Screen
   */
  val currentStatus = snapshotFlow { uiState.statusList }
    .filter { it.isNotEmpty() }
    .distinctUntilChangedBy { list ->
      list.first { it.id == navArgs.status.id }
    }
    .map { list ->
      list.first { it.id == navArgs.status.id }
    }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.Eagerly,
      initialValue = navArgs.status.toUiData()
    )

  init {
    uiState = uiState.copy(
      threadList = listOf(
        ReplyThread(
          content = generateHtmlContentWithEmoji(navArgs.status.content, navArgs.status.emojis),
          account = navArgs.status.account,
          selected = true
        )
      )
    )
    var latestStatus = navArgs.status.toUiData()

    viewModelScope.launch {
      val accountId = accountDao.getActiveAccount()!!.accountId
      uiState = uiState.copy(
        loading = true,
        statusList = persistentListOf(latestStatus),
        instanceEmojis = instanceRepository.getInstanceEmojis().toImmutableList(),
      )
      val latestStatusDeferred = async(Dispatchers.IO) {
        statusRepository.getSingleStatus(navArgs.status.id)
      }.await()

      val statusContextDeferred = async(Dispatchers.IO) {
        statusRepository.getStatusContext(navArgs.status.id)
      }.await()

      latestStatusDeferred
        .onSuccess {
          latestStatus = it.toUiData()
          updateStatusInDatabase(it)
        }
        .onFailure {
          it.printStackTrace()
          timelineUseCase.onStatusLoadError(it)
          if (it.code == 404) {
            val dbId = accountDao.getActiveAccount()!!.id
            val isInDb = timelineDao.getSingleStatusWithId(dbId, navArgs.status.id) != null
            if (isInDb) timelineDao.clear(dbId, navArgs.status.id)
          }
        }

      statusContextDeferred
        .onSuccess { statusContext ->
          val combinedList = (statusContext.ancestors.toUiData() +
            latestStatus + reorderDescendants(statusContext.descendants)).toImmutableList()
          val threadList = statusContext.ancestors
            .filter { it.account.id != navArgs.status.account.id && it.account.id != accountId }
            .reversed()
            .distinctBy { it.account.id }
            .map {
              ReplyThread(
                content = generateHtmlContentWithEmoji(it.content, it.emojis),
                account = it.account,
                selected = false
              )
            }
            .reversed() + uiState.threadList
          uiState = uiState.copy(
            loading = false,
            statusList = combinedList,
            threadList = threadList
          )
        }
        .onFailure {
          uiState = uiState.copy(loading = false)
          it.printStackTrace()
        }
    }
  }

  fun onStatusAction(action: StatusAction, id: String) = viewModelScope.launch {
    uiState = uiState.copy(
      statusList = updateStatusListActions(uiState.statusList, action, id).toImmutableList()
    )
    timelineUseCase.onStatusAction(action)?.let {
      if (action is VotePoll && it.isSuccess) {
        val targetStatus = it.getOrNull()!!
        uiState = uiState.copy(
          statusList = updatePollOfStatusList(
            statusList = uiState.statusList,
            targetId = targetStatus.id,
            poll = targetStatus.poll!!
          ).toImmutableList()
        )
      }
    }
    updateStatusInDatabase()
  }

  fun updateStatusFromDetailScreen(newStatus: StatusBackResult) {
    uiState = uiState.copy(
      statusList = uiState.statusList.updateStatusActionData(newStatus).toImmutableList()
    )
  }

  fun replyToStatus() {
    if (uiState.postState == PostState.Posting) return
    uiState = uiState.copy(postState = PostState.Posting)
    viewModelScope.launch {
      statusRepository.createStatus(
        content = when (uiState.threadList.size) {
          1 -> "${navArgs.status.account.fullname} ${replyField.text}"
          else -> uiState.threadList.joinToString(separator = " ") { it.account.fullname } +
            " ${replyField.text}"
        },
        inReplyToId = navArgs.status.actionableId,
      )
        .catch {
          it.printStackTrace()
          uiState = uiState.copy(postState = PostState.Failure(it))
        }
        .collect { response ->
          uiState = uiState.copy(
            postState = PostState.Success,
            statusList = uiState.statusList.toMutableList().also {
              it.add(
                index = it.indexOfFirst { item -> item.id == navArgs.status.id } + 1,
                element = response.toUiData()
              )
            }.toImmutableList(),
          )
          replyField.clearText()
          delay(50)
          uiState = uiState.copy(postState = PostState.Idle)
        }
    }
  }

  fun updateThreads(index: Int, selected: Boolean) {
    if (index == uiState.threadList.lastIndex) return
    uiState = uiState.copy(
      threadList = uiState.threadList.mapIndexed { i, thread ->
        if (i == index) thread.copy(selected = selected) else thread
      }
    )
  }

  /**
   * we need sync the latest status data to database, because if the user performs some action
   * from detail screen, the status data in database will be outdated.
   * if we update the status data in database, the timeline screen will be the latest data when user
   * back to timeline screen
   *
   * @param status Update the source of the data. If it is empty,
   * use part of the StatusUiData in the current memory.
   */
  private fun updateStatusInDatabase(status: Status? = null) {
    // if origin status id is null, it means the current status if not from timeline screen
    // so we don't need to update the status in database
    if (navArgs.originStatusId == null) return
    viewModelScope.launch {
      val activeAccountId = accountDao.getActiveAccount()!!.id
      var savedStatus = timelineDao.getSingleStatusWithId(activeAccountId, navArgs.originStatusId)
      val newStatus = when (status) {
        null -> {
          navArgs.status.copy(
            favorited = currentStatus.value.favorited,
            favouritesCount = currentStatus.value.favouritesCount,
            reblogged = currentStatus.value.reblogged,
            reblogsCount = currentStatus.value.reblogsCount,
            bookmarked = currentStatus.value.bookmarked,
            poll = currentStatus.value.poll
          )
        }
        else -> status
      }
      savedStatus?.let {
        savedStatus = when (it.reblog == null) {
          true -> newStatus
          else -> it.copy(reblog = newStatus)
        }
        timelineDao.insertOrUpdate(savedStatus!!.toEntity(activeAccountId))
      }
    }
  }

  private fun reorderDescendants(descendants: List<Status>): ImmutableList<StatusUiData> {
    if (descendants.isEmpty() || descendants.size == 1)
      return descendants.toUiData().toImmutableList()

    // remove some replies that did not reply to the current Status
    val replyList = descendants.filter { it.inReplyToId == navArgs.status.actionableId }
    val finalList = mutableListOf<Status>()

    fun searchSubReplies(current: String): List<Status> {
      val subReplies = mutableListOf<Status>()
      var now = current
      descendants.forEach {
        if (it.inReplyToId == now) {
          subReplies.add(it)
          now = it.id
        }
      }
      return subReplies
    }
    replyList.forEach { current ->
      val subReplies = searchSubReplies(current.id).toMutableList()
      if (subReplies.isNotEmpty()) {
        subReplies.add(0, current)
        finalList.addAll(subReplies)
      } else {
        finalList.add(current)
      }
    }
    return finalList.toUiData().toImmutableList()
  }
}

@Immutable
data class StatusDetailUiState(
  val loading: Boolean = false,
  val threadList: List<ReplyThread> = emptyList(),
  val instanceEmojis: ImmutableList<Emoji> = persistentListOf(),
  val statusList: ImmutableList<StatusUiData> = persistentListOf(),
  val postState: PostState = PostState.Idle
)
