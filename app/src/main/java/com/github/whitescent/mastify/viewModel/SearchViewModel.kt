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
import com.github.whitescent.mastify.data.repository.SearchRepository
import com.github.whitescent.mastify.extensions.updateStatusActionData
import com.github.whitescent.mastify.mapper.toUiData
import com.github.whitescent.mastify.network.model.account.Account
import com.github.whitescent.mastify.network.model.status.Hashtag
import com.github.whitescent.mastify.network.model.status.Status
import com.github.whitescent.mastify.paging.Paginator
import com.github.whitescent.mastify.paging.factory.SearchPagingFactory
import com.github.whitescent.mastify.screen.explore.SearchNavigateType
import com.github.whitescent.mastify.screen.navArgs
import com.github.whitescent.mastify.screen.search.SearchResultNavArgs
import com.github.whitescent.mastify.usecase.TimelineUseCase
import com.github.whitescent.mastify.usecase.TimelineUseCase.Companion.updateStatusListActions
import com.github.whitescent.mastify.utils.StatusAction
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
class SearchViewModel @Inject constructor(
  savedStateHandle: SavedStateHandle,
  repository: SearchRepository,
  private val timelineUseCase: TimelineUseCase
) : ViewModel() {

  val navArgs: SearchResultNavArgs = savedStateHandle.navArgs()

  private val statusPagingFactory = SearchPagingFactory<Status>(
    keyword = navArgs.searchQuery,
    repository = repository,
    type = SearchType.Status
  )

  private val accountPagingFactory = SearchPagingFactory<Account>(
    keyword = navArgs.searchQuery,
    repository = repository,
    type = SearchType.Account
  )

  private val hashtagsPagingFactory = SearchPagingFactory<Hashtag>(
    keyword = navArgs.searchQuery,
    repository = repository,
    type = SearchType.Hashtag
  )

  val statusList = statusPagingFactory.list
    .map { it.toUiData() }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.Eagerly,
      initialValue = emptyList()
    )

  val accounts = accountPagingFactory.list
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.Eagerly,
      initialValue = emptyList()
    )

  val hashtags = hashtagsPagingFactory.list
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.Eagerly,
      initialValue = emptyList()
    )

  val statusPaginator = Paginator(
    pageSize = PAGE_SIZE,
    initRefresh = navArgs.searchType == null,
    pagingFactory = statusPagingFactory
  )

  val accountsPaginator = Paginator(
    pageSize = PAGE_SIZE,
    initRefresh = (navArgs.searchType != null && navArgs.searchType == SearchNavigateType.Account),
    pagingFactory = accountPagingFactory
  )

  val hashtagsPaginator = Paginator(
    pageSize = PAGE_SIZE,
    initRefresh = (navArgs.searchType != null && navArgs.searchType == SearchNavigateType.Tags),
    pagingFactory = hashtagsPagingFactory
  )

  var uiState by mutableStateOf(SearchUiState(""))
    private set

  private var currentSearchTypeFlow = MutableStateFlow(
    if (navArgs.searchType != null) {
      when (navArgs.searchType) {
        SearchNavigateType.Account -> SearchType.Account
        SearchNavigateType.Tags -> SearchType.Hashtag
      }
    } else SearchType.Status
  )

  val currentSearchType = currentSearchTypeFlow
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.Eagerly,
      initialValue = currentSearchTypeFlow.value
    )

  fun updateQuery(str: String) { uiState = uiState.copy(query = str) }
  fun clearQuery() { uiState = uiState.copy(query = "") }

  fun updateSearchType(type: Int) {
    currentSearchTypeFlow.value = SearchType.entries.toTypedArray()[type]
  }

  fun onStatusAction(action: StatusAction, status: Status) {
    viewModelScope.launch(Dispatchers.IO) {
      statusPagingFactory.list.update {
        updateStatusListActions(it, action, status.id)
      }
      timelineUseCase.onStatusAction(action)?.let { response ->
        if (action is StatusAction.VotePoll && response.isSuccess) {
          val targetStatus = response.getOrNull()!!
          statusPagingFactory.list.update {
            TimelineUseCase.updatePollOfStatusList(
              it,
              targetStatus.id,
              targetStatus.poll!!
            )
          }
        }
      }
    }
  }

  fun updateStatusFromDetailScreen(newStatus: StatusBackResult) {
    val statusList = statusPagingFactory.list.value
    statusPagingFactory.list.value = statusList.updateStatusActionData(newStatus)
  }

  init {
    uiState = uiState.copy(query = navArgs.searchQuery)
    viewModelScope.launch {
      currentSearchTypeFlow.collect {
        when (it) {
          SearchType.Status -> if (statusList.value.isEmpty()) statusPaginator.refresh()
          SearchType.Account -> if (accounts.value.isEmpty()) accountsPaginator.refresh()
          SearchType.Hashtag -> if (hashtags.value.isEmpty()) hashtagsPaginator.refresh()
        }
      }
    }
  }

  companion object {
    const val PAGE_SIZE = 20
  }
}

enum class SearchType(
  @StringRes val label: Int
) {
  Status(R.string.post_title),
  Account(R.string.accounts_title),
  Hashtag(R.string.hashtags_title);

  override fun toString(): String {
    return when (this) {
      Status -> "statuses"
      Account -> "accounts"
      Hashtag -> "hashtags"
    }
  }
}

data class SearchUiState(
  val query: String = ""
)
