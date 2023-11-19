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
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.github.whitescent.R
import com.github.whitescent.mastify.data.model.ui.StatusUiData
import com.github.whitescent.mastify.data.repository.ExploreRepository
import com.github.whitescent.mastify.data.repository.SearchPreviewResult
import com.github.whitescent.mastify.database.AppDatabase
import com.github.whitescent.mastify.domain.StatusActionHandler
import com.github.whitescent.mastify.network.model.search.SearchResult
import com.github.whitescent.mastify.utils.StatusAction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class ExplorerViewModel @Inject constructor(
  db: AppDatabase,
  private val statusActionHandler: StatusActionHandler,
  private val exploreRepository: ExploreRepository,
) : ViewModel() {

  private val accountDao = db.accountDao()
  private val activityAccountFlow = accountDao.getActiveAccountFlow()

  private val searchErrorChannel = Channel<Unit>()
  val searchErrorFlow = searchErrorChannel.receiveAsFlow()

  val snackBarFlow = statusActionHandler.snackBarFlow

  var uiState by mutableStateOf(ExploreUiState())
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

  val trendingStatusPager: Flow<PagingData<StatusUiData>> = activityAccountFlow
    .filterNotNull()
    .map { it.id }
    .distinctUntilChanged()
    .flatMapLatest { exploreRepository.getTrendingStatusPager() }
    .cachedIn(viewModelScope)

  val publicTimelinePager: Flow<PagingData<StatusUiData>> = activityAccountFlow
    .filterNotNull()
    .map { it.id }
    .distinctUntilChanged()
    .flatMapLatest { exploreRepository.getPublicTimelinePager() }
    .cachedIn(viewModelScope)

  init {
    viewModelScope.launch {
      activityAccountFlow.collect {
        it?.let {
          uiState = uiState.copy(
            avatar = it.profilePictureUrl,
            userInstance = it.domain
          )
        }
      }
    }
  }

  fun onValueChange(text: String) {
    uiState = uiState.copy(text = text)
  }

  fun clearInputText() {
    uiState = uiState.copy(text = "")
  }

  fun onStatusAction(action: StatusAction, context: Context) = viewModelScope.launch {
    statusActionHandler.onStatusAction(action, context)
  }
}

data class ExploreUiState(
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
