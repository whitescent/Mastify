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
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.connyduck.calladapter.networkresult.fold
import com.github.whitescent.mastify.data.model.StatusBackResult
import com.github.whitescent.mastify.data.model.ui.StatusUiData
import com.github.whitescent.mastify.data.repository.InstanceRepository
import com.github.whitescent.mastify.database.AppDatabase
import com.github.whitescent.mastify.domain.StatusActionHandler
import com.github.whitescent.mastify.domain.StatusActionHandler.Companion.updatePollOfStatusList
import com.github.whitescent.mastify.domain.StatusActionHandler.Companion.updateStatusListActions
import com.github.whitescent.mastify.extensions.updateStatusActionData
import com.github.whitescent.mastify.mapper.status.toEntity
import com.github.whitescent.mastify.mapper.status.toUiData
import com.github.whitescent.mastify.network.MastodonApi
import com.github.whitescent.mastify.network.model.emoji.Emoji
import com.github.whitescent.mastify.network.model.status.NewStatus
import com.github.whitescent.mastify.network.model.status.Status
import com.github.whitescent.mastify.screen.navArgs
import com.github.whitescent.mastify.screen.other.StatusDetailNavArgs
import com.github.whitescent.mastify.utils.PostState
import com.github.whitescent.mastify.utils.StatusAction
import com.github.whitescent.mastify.utils.StatusAction.VotePoll
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class StatusDetailViewModel @Inject constructor(
  savedStateHandle: SavedStateHandle,
  db: AppDatabase,
  private val api: MastodonApi,
  private val statusActionHandler: StatusActionHandler,
  private val instanceRepository: InstanceRepository
) : ViewModel() {

  private val timelineDao = db.timelineDao()
  private val accountDao = db.accountDao()

  val snackBarFlow = statusActionHandler.snackBarFlow
  val navArgs: StatusDetailNavArgs = savedStateHandle.navArgs()

  var replyField by mutableStateOf(TextFieldValue(""))
    private set

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

  fun onStatusAction(action: StatusAction, context: Context, id: String) = viewModelScope.launch {
    uiState = uiState.copy(
      statusList = updateStatusListActions(uiState.statusList, action, id).toImmutableList()
    )
    statusActionHandler.onStatusAction(action, context)?.let {
      if (action is VotePoll) {
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
    uiState = uiState.copy(postState = PostState.Posting)
    viewModelScope.launch {
      api.createStatus(
        idempotencyKey = UUID.randomUUID().toString(),
        status = NewStatus(
          status = "${navArgs.status.account.fullname} ${replyField.text}",
          warningText = "",
          inReplyToId = navArgs.status.actionableId,
          visibility = "public", // TODO
          sensitive = false, // TODO
          mediaIds = null,
          mediaAttributes = null,
          scheduledAt = null,
          poll = null,
          language = null,
        ),
      ).fold(
        { status ->
          uiState = uiState.copy(
            postState = PostState.Success,
            statusList = uiState.statusList.toMutableList().also {
              it.add(
                index = it.indexOfFirst { item -> item.id == navArgs.status.id } + 1,
                element = status.toUiData()
              )
            }.toImmutableList(),
          )
          replyField = replyField.copy(text = "")
          delay(50)
          uiState = uiState.copy(postState = PostState.Idle)
        },
        {
          it.printStackTrace()
          uiState = uiState.copy(postState = PostState.Failure)
        }
      )
    }
  }

  init {
    var latestStatus = navArgs.status.toUiData()
    uiState = uiState.copy(loading = true, statusList = persistentListOf(latestStatus))
    viewModelScope.launch {
      api.status(navArgs.status.id).fold(
        {
          latestStatus = it.toUiData() // fetch latest status data
        },
        {
          statusActionHandler.onStatusLoadError()
        }
      )
      api.statusContext(navArgs.status.id).fold(
        {
          val combinedList = (it.ancestors.toUiData() +
            latestStatus + reorderDescendants(it.descendants)).toImmutableList()
          uiState = uiState.copy(
            loading = false,
            instanceEmojis = instanceRepository.getEmojis().toImmutableList(),
            statusList = combinedList
          )
          updateStatusInDatabase()
        },
        {
          uiState = uiState.copy(loading = false)
          it.printStackTrace()
        }
      )
    }
  }

  fun updateTextFieldValue(textFieldValue: TextFieldValue) { replyField = textFieldValue }

  /**
   * we need sync the latest status data to database, because if the user performs some action
   * from detail screen, the status data in database will be outdated.
   * if we update the status data in database, the timeline screen will be the latest data when user
   * back to timeline screen
   */
  private fun updateStatusInDatabase() {
    // if origin status id is null, it means the currentStatus.value if not from timeline screen
    // so we don't need to update the status in database
    if (navArgs.originStatusId == null) return
    viewModelScope.launch {
      val activeAccountId = accountDao.getActiveAccount()!!.id
      var savedStatus = timelineDao.getSingleStatusWithId(activeAccountId, navArgs.originStatusId)
      val newStatus = navArgs.status.copy(
        favorited = currentStatus.value.favorited,
        favouritesCount = currentStatus.value.favouritesCount,
        reblogged = currentStatus.value.reblogged,
        reblogsCount = currentStatus.value.reblogsCount,
        bookmarked = currentStatus.value.bookmarked,
        poll = currentStatus.value.poll
      )
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

    // remove some replies that did not reply to the currentStatus
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
  val instanceEmojis: ImmutableList<Emoji> = persistentListOf(),
  val statusList: ImmutableList<StatusUiData> = persistentListOf(),
  val postState: PostState = PostState.Idle
)
