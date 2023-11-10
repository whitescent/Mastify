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
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.github.whitescent.R
import com.github.whitescent.mastify.data.repository.AccountRepository
import com.github.whitescent.mastify.data.repository.ExploreRepository
import com.github.whitescent.mastify.data.repository.SearchPreviewResult
import com.github.whitescent.mastify.database.AppDatabase
import com.github.whitescent.mastify.domain.StatusActionHandler
import com.github.whitescent.mastify.network.MastodonApi
import com.github.whitescent.mastify.network.model.search.SearchResult
import com.github.whitescent.mastify.paging.PublicTimelinePagingSource
import com.github.whitescent.mastify.paging.TrendingPagingSource
import com.github.whitescent.mastify.utils.StatusAction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class ExplorerViewModel @Inject constructor(
  private val db: AppDatabase,
  private val api: MastodonApi,
  private val statusActionHandler: StatusActionHandler,
  private val exploreRepository: ExploreRepository,
  private val accountRepository: AccountRepository,
) : ViewModel() {

  private val activityAccount get() = accountRepository.activeAccount!!

  private val searchErrorChannel = Channel<Unit>()
  val searchErrorFlow = searchErrorChannel.receiveAsFlow()

  val snackBarFlow = statusActionHandler.snackBarFlow

  var uiState by mutableStateOf(ExplorerUiState())
    private set

  val searchPreviewResult: StateFlow<SearchResult?> =
    snapshotFlow { uiState.text }
      .debounce(200)
      .mapLatest {
        // reset search response when query is empty
        when (val api = exploreRepository.getPreviewResultsForSearch(it)) {
          is SearchPreviewResult.Success -> api.response
          is SearchPreviewResult.Failure -> {
            searchErrorChannel.send(Unit)
            null
          }
        }
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null
      )

  val trendingStatusPager = Pager(
    config = PagingConfig(
      pageSize = 20,
      enablePlaceholders = false,
    ),
    pagingSourceFactory = {
      TrendingPagingSource(api = api)
    },
  ).flow.cachedIn(viewModelScope)

  val publicTimelinePager = Pager(
    config = PagingConfig(
      pageSize = 20,
      enablePlaceholders = false,
    ),
    pagingSourceFactory = {
      PublicTimelinePagingSource(api = api)
    },
  ).flow.cachedIn(viewModelScope)

  init {
    uiState = uiState.copy(
      avatar = activityAccount.profilePictureUrl,
      userInstance = activityAccount.domain
    )
  }

  fun onValueChange(text: String) {
    uiState = uiState.copy(text = text)
  }

  fun onStatusAction(action: StatusAction, context: Context) = viewModelScope.launch {
    statusActionHandler.onStatusAction(action, context)
  }
}

data class ExplorerUiState(
  val avatar: String = "",
  val text: String = "",
  val userInstance: String = ""
)

enum class ExplorerKind(
  @StringRes val stringRes: Int
) {
  Trending(R.string.trending_title),
  PublicTimeline(R.string.public_timeline),
  News(R.string.news_title)
}
