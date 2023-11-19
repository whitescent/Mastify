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
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.connyduck.calladapter.networkresult.fold
import com.github.whitescent.mastify.data.model.ui.StatusUiData
import com.github.whitescent.mastify.data.repository.InstanceRepository
import com.github.whitescent.mastify.database.AppDatabase
import com.github.whitescent.mastify.domain.StatusActionHandler
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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
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

  private var isInitialLoad = false
  private val timelineDao = db.timelineDao()
  private val accountDao = db.accountDao()

  val snackBarFlow = statusActionHandler.snackBarFlow
  val navArgs: StatusDetailNavArgs = savedStateHandle.navArgs()

  var replyField by mutableStateOf(TextFieldValue(""))
    private set

  var uiState by mutableStateOf(StatusDetailUiState())
    private set

  var currentStatus by mutableStateOf(navArgs.status.toUiData())
    private set

  fun onStatusAction(action: StatusAction, context: Context) = viewModelScope.launch {
    when (action) {
      is StatusAction.Favorite -> {
        if (action.id == currentStatus.id) {
          currentStatus = currentStatus.copy(
            favorited = action.favorite,
            favouritesCount = when (action.favorite) {
              true -> currentStatus.favouritesCount + 1
              else -> currentStatus.favouritesCount - 1
            }
          )
          updateStatusInDatabase()
        }
      }
      is StatusAction.Reblog -> {
        if (action.id == currentStatus.id) {
          currentStatus = currentStatus.copy(
            reblogged = action.reblog,
            reblogsCount = when (action.reblog) {
              true -> currentStatus.reblogsCount + 1
              else -> currentStatus.reblogsCount - 1
            }
          )
        }
        updateStatusInDatabase()
      }
      is StatusAction.Bookmark -> {
        if (action.id == currentStatus.id) {
          currentStatus = currentStatus.copy(bookmarked = action.bookmark)
          updateStatusInDatabase()
        }
      }
      else -> Unit
    }
    statusActionHandler.onStatusAction(action, context)
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
            descendants = uiState.descendants.toMutableList().also {
              it.add(0, status.toUiData())
            }.toImmutableList()
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
    currentStatus = currentStatus.copy(repliesCount = currentStatus.repliesCount + 1)
    updateStatusInDatabase()
  }

  init {
    uiState = uiState.copy(loading = true)
    viewModelScope.launch {
      api.status(navArgs.status.id).fold(
        {
          currentStatus = it.toUiData() // fetch latest currentStatus
          updateStatusInDatabase()
        },
        {
          statusActionHandler.onStatusLoadError()
        }
      )
      api.statusContext(navArgs.status.actionableId).fold(
        {
          uiState = uiState.copy(
            loading = false,
            instanceEmojis = instanceRepository.getEmojis().toImmutableList(),
            ancestors = it.ancestors.toUiData().toImmutableList(),
            descendants = reorderDescendants(it.descendants),
          )
          isInitialLoad = true
        },
        {
          it.printStackTrace()
          uiState = uiState.copy(loading = false, loadError = true)
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
    // if origin status id is null, it means the currentStatus if not from timeline screen
    // so we don't need to update the status in database
    if (navArgs.originStatusId == null) return
    viewModelScope.launch {
      val activeAccountId = accountDao.getActiveAccount()!!.id
      var savedStatus =
        timelineDao.getSingleStatusWithId(activeAccountId, navArgs.originStatusId)
      savedStatus?.let {
        when (it.reblog == null) {
          true -> {
            savedStatus = navArgs.status.copy(
              favorited = currentStatus.favorited,
              favouritesCount = currentStatus.favouritesCount,
              reblog = currentStatus.reblog,
              reblogged = currentStatus.reblogged,
              bookmarked = currentStatus.bookmarked,
              reblogsCount = currentStatus.reblogsCount,
              repliesCount = currentStatus.repliesCount,
            )
          }
          else -> {
            savedStatus = it.copy(
              reblog = navArgs.status.copy(
                favorited = currentStatus.favorited,
                favouritesCount = currentStatus.favouritesCount,
                reblog = currentStatus.reblog,
                reblogged = currentStatus.reblogged,
                bookmarked = currentStatus.bookmarked,
                reblogsCount = currentStatus.reblogsCount,
                repliesCount = currentStatus.repliesCount,
              )
            )
          }
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
  val ancestors: ImmutableList<StatusUiData> = persistentListOf(),
  val descendants: ImmutableList<StatusUiData> = persistentListOf(),
  val loadError: Boolean = false,
  val postState: PostState = PostState.Idle
)
