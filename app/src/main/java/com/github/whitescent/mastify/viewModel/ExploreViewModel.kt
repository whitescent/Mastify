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
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.whitescent.R
import com.github.whitescent.mastify.data.model.StatusBackResult
import com.github.whitescent.mastify.data.repository.ExploreRepository
import com.github.whitescent.mastify.database.AppDatabase
import com.github.whitescent.mastify.extensions.updateStatusActionData
import com.github.whitescent.mastify.network.model.search.SearchResult
import com.github.whitescent.mastify.network.model.status.Hashtag
import com.github.whitescent.mastify.network.model.status.Status
import com.github.whitescent.mastify.network.model.trends.News
import com.github.whitescent.mastify.paging.Paginator
import com.github.whitescent.mastify.paging.factory.ExplorePagingFactory
import com.github.whitescent.mastify.usecase.TimelineUseCase
import com.github.whitescent.mastify.usecase.TimelineUseCase.Companion.updatePollOfStatusList
import com.github.whitescent.mastify.usecase.TimelineUseCase.Companion.updateStatusListActions
import com.github.whitescent.mastify.utils.StatusAction
import com.github.whitescent.mastify.viewModel.ExplorerKind.PublicTimeline
import com.github.whitescent.mastify.viewModel.ExplorerKind.Trending
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class ExploreViewModel @Inject constructor(
  db: AppDatabase,
  private val timelineUseCase: TimelineUseCase,
  private val exploreRepository: ExploreRepository,
) : ViewModel() {

  private val accountDao = db.accountDao()

  val activityAccountFlow = accountDao
    .getActiveAccountFlow()
    .distinctUntilChanged { old, new -> old?.id == new?.id }
    .filterNotNull()
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.Eagerly,
      initialValue = null
    )

  private var currentExploreKindFlow = MutableStateFlow(Trending)

  private val trendingPagingFactory =
    ExplorePagingFactory<Status>(Trending, exploreRepository)

  private val publicTimelinePagingFactory =
    ExplorePagingFactory<Status>(PublicTimeline, exploreRepository)

  private val newsPagingFactory =
    ExplorePagingFactory<News>(ExplorerKind.News, exploreRepository)

  val currentExploreKind = currentExploreKindFlow
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.Eagerly,
      initialValue = Trending
    )

  val trending = trendingPagingFactory.list
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.Eagerly,
      initialValue = emptyList()
    )

  val publicTimeline = publicTimelinePagingFactory.list
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.Eagerly,
      initialValue = emptyList()
    )

  val news = newsPagingFactory.list
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.Eagerly,
      initialValue = emptyList()
    )

  val trendingPaginator = Paginator(
    pageSize = EXPLOREPAGINGFETCHNUMBER,
    pagingFactory = trendingPagingFactory
  )

  val publicTimelinePaginator = Paginator(
    pageSize = EXPLOREPAGINGFETCHNUMBER,
    pagingFactory = publicTimelinePagingFactory
  )

  val newsPaginator = Paginator(
    pageSize = EXPLOREPAGINGFETCHNUMBER,
    pagingFactory = newsPagingFactory
  )

  private val searchErrorChannel = Channel<Unit>()
  val searchErrorFlow = searchErrorChannel.receiveAsFlow()

  val snackBarFlow = timelineUseCase.snackBarFlow

  var uiState by mutableStateOf(ExploreUiState())
    private set

  val searchPreviewResult: StateFlow<SearchResult?> =
    snapshotFlow { uiState.text }
      .debounce(200)
      .mapLatest {
        // reset search response when query is empty
        val api = exploreRepository.getPreviewResultsForSearch(it)
        api.getOrNull().also {
          if (api.isFailure) searchErrorChannel.send(Unit)
        }
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null
      )

  fun onValueChange(text: String) {
    uiState = uiState.copy(text = text)
  }

  fun clearInputText() {
    uiState = uiState.copy(text = "")
  }

  fun onStatusAction(action: StatusAction, kind: ExplorerKind, status: Status) {
    viewModelScope.launch(Dispatchers.IO) {
      when (kind) {
        Trending -> trendingPagingFactory.list.update {
          updateStatusListActions(it, action, status.id)
        }
        PublicTimeline -> publicTimelinePagingFactory.list.update {
          updateStatusListActions(it, action, status.id)
        }
        else -> Unit
      }
      timelineUseCase.onStatusAction(action)?.let { response ->
        if (action is StatusAction.VotePoll) {
          val targetStatus = response.getOrNull()!!
          trendingPagingFactory.list.update {
            updatePollOfStatusList(it, targetStatus.id, targetStatus.poll!!)
          }
          publicTimelinePagingFactory.list.update {
            updatePollOfStatusList(it, targetStatus.id, targetStatus.poll!!)
          }
        }
      }
    }
  }

  fun updateStatusFromDetailScreen(newStatus: StatusBackResult) {
    val trending = trendingPagingFactory.list.value
    val publicTimeline = publicTimelinePagingFactory.list.value
    trendingPagingFactory.list.value = trending.updateStatusActionData(newStatus)
    publicTimelinePagingFactory.list.value = publicTimeline.updateStatusActionData(newStatus)
  }

  fun syncExploreKind(page: Int) {
    currentExploreKindFlow.value = ExplorerKind.entries.toTypedArray()[page]
  }

  companion object {
    const val EXPLOREPAGINGFETCHNUMBER = 20
  }
}

data class ExploreUiState(
  val text: String = "",
  val trendingNews: List<News>? = null,
  val topics: List<Hashtag> = emptyList()
)

enum class ExplorerKind(
  @StringRes val stringRes: Int
) {
  Trending(R.string.trending_title),
  PublicTimeline(R.string.public_timeline),
  News(R.string.news_title)
}
