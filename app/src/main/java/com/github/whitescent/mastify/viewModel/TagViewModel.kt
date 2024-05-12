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
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.whitescent.mastify.data.model.StatusBackResult
import com.github.whitescent.mastify.data.repository.TagRepository
import com.github.whitescent.mastify.extensions.updateStatusActionData
import com.github.whitescent.mastify.mapper.toUiData
import com.github.whitescent.mastify.network.model.status.Status
import com.github.whitescent.mastify.paging.Paginator
import com.github.whitescent.mastify.paging.factory.TagPagingFactory
import com.github.whitescent.mastify.screen.other.TagInfoNavArgs
import com.github.whitescent.mastify.usecase.TimelineUseCase
import com.github.whitescent.mastify.usecase.TimelineUseCase.Companion.updateStatusListActions
import com.github.whitescent.mastify.utils.PostState
import com.github.whitescent.mastify.utils.PostState.Failure
import com.github.whitescent.mastify.utils.PostState.Success
import com.github.whitescent.mastify.utils.StatusAction
import com.ramcosta.composedestinations.generated.navArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TagViewModel @Inject constructor(
  savedStateHandle: SavedStateHandle,
  private val timelineUseCase: TimelineUseCase,
  private val repository: TagRepository
) : ViewModel() {

  val navArgs: TagInfoNavArgs = savedStateHandle.navArgs()

  private val tagPagingFactory = TagPagingFactory(navArgs.name, repository)

  var uiState by mutableStateOf(TagUiState())
    private set

  val tagPaginator = Paginator(
    pageSize = PAGE_SIZE,
    pagingFactory = tagPagingFactory
  )

  val tagTimeline = tagPagingFactory.list
    .map { it.toUiData() }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.Eagerly,
      initialValue = emptyList()
    )

  init {
    uiState = uiState.copy(hashtag = navArgs.name)
    viewModelScope.launch {
      repository.fetchHashtagInfo(navArgs.name)
        .catch {}
        .collect {
          uiState = uiState.copy(
            posts = it.posts,
            participants = it.participants,
            postsToday = it.todayPost,
            following = it.following
          )
        }
    }
  }

  fun onStatusAction(action: StatusAction, status: Status) {
    viewModelScope.launch(Dispatchers.IO) {
      tagPagingFactory.list.update {
        updateStatusListActions(it, action, status.id)
      }
      timelineUseCase.onStatusAction(action)?.let { response ->
        if (action is StatusAction.VotePoll && response.isSuccess) {
          val targetStatus = response.getOrNull()!!
          tagPagingFactory.list.update {
            TimelineUseCase.updatePollOfStatusList(it, targetStatus.id, targetStatus.poll!!)
          }
        }
      }
    }
  }

  fun followHashtag() {
    uiState = uiState.copy(followState = PostState.Posting)
    viewModelScope.launch {
      repository.followHashtag(uiState.hashtag, !uiState.following!!)
        .catch { uiState = uiState.copy(followState = Failure(it)) }
        .collect {
          uiState = uiState.copy(followState = Success, following = it.following)
        }
    }
  }

  fun updateStatusFromDetailScreen(newStatus: StatusBackResult) {
    val statusList = tagPagingFactory.list.value
    tagPagingFactory.list.value = statusList.updateStatusActionData(newStatus)
  }

  companion object {
    const val PAGE_SIZE = 20
  }
}

data class TagUiState(
  val hashtag: String = "",
  val posts: Int = 0,
  val participants: Int = 0,
  val postsToday: Int = 0,
  val following: Boolean? = null,
  val followState: PostState = PostState.Idle
)
