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
import at.connyduck.calladapter.networkresult.getOrDefault
import com.github.whitescent.R
import com.github.whitescent.mastify.data.model.StatusBackResult
import com.github.whitescent.mastify.data.model.ui.StatusCommonListData
import com.github.whitescent.mastify.data.model.ui.StatusUiData
import com.github.whitescent.mastify.data.repository.ExploreRepository
import com.github.whitescent.mastify.database.AppDatabase
import com.github.whitescent.mastify.domain.StatusActionHandler
import com.github.whitescent.mastify.domain.StatusActionHandler.Companion.updateStatusListActions
import com.github.whitescent.mastify.extensions.updateStatusActionData
import com.github.whitescent.mastify.mapper.status.toUiData
import com.github.whitescent.mastify.network.MastodonApi
import com.github.whitescent.mastify.network.model.search.SearchResult
import com.github.whitescent.mastify.network.model.status.Hashtag
import com.github.whitescent.mastify.network.model.status.Status
import com.github.whitescent.mastify.network.model.trends.News
import com.github.whitescent.mastify.paging.LoadState
import com.github.whitescent.mastify.paging.Paginator
import com.github.whitescent.mastify.utils.StatusAction
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
class ExplorerViewModel @Inject constructor(
  db: AppDatabase,
  private val api: MastodonApi,
  private val statusActionHandler: StatusActionHandler,
  private val exploreRepository: ExploreRepository,
) : ViewModel() {

  private val accountDao = db.accountDao()
  private val activityAccountFlow = accountDao
    .getActiveAccountFlow()
    .distinctUntilChanged { old, new -> old?.id == new?.id }
    .filterNotNull()

  private var trendingFlow = MutableStateFlow(StatusCommonListData<StatusUiData>())
  private var publicTimelineFlow = MutableStateFlow(StatusCommonListData<StatusUiData>())
  private var currentExploreKindFlow = MutableStateFlow(ExplorerKind.Trending)

  val currentExploreKind = currentExploreKindFlow
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.Eagerly,
      initialValue = ExplorerKind.Trending
    )

  val trending = trendingFlow
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.Eagerly,
      initialValue = StatusCommonListData()
    )

  val publicTimeline = publicTimelineFlow
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.Eagerly,
      initialValue = StatusCommonListData()
    )

  val trendingPaginator = Paginator(
    refreshKey = 0,
    getAppendKey = {
      trendingFlow.value.timeline.size
    },
    onLoadUpdated = {
      trendingFlow.value = trendingFlow.value.copy(loadState = it)
    },
    onError = { it?.printStackTrace() },
    onRequest = { nextPage ->
      val response = api.trendingStatus(offset = nextPage)
      if (response.isSuccessful && !response.body().isNullOrEmpty()) {
        val body = response.body()!!
        Result.success(body)
      } else Result.success(emptyList())
    },
    onSuccess = { state, item ->
      when (state) {
        LoadState.Append -> {
          val timeline = trendingFlow.value.timeline
          trendingFlow.emit(
            trendingFlow.value.copy(
              timeline = timeline + item.toUiData(),
              endReached = item.isEmpty()
            )
          )
        }
        LoadState.Refresh -> {
          trendingFlow.emit(
            trendingFlow.value.copy(
              timeline = item.toUiData(),
              endReached = item.isEmpty() || item.size < EXPLOREPAGINGFETCHNUMBER
            )
          )
        }
        else -> Unit
      }
    }
  )

  val publicTimelinePaginator = Paginator(
    refreshKey = null,
    getAppendKey = {
      publicTimelineFlow.value.timeline.lastOrNull()?.id
    },
    onLoadUpdated = {
      publicTimelineFlow.value = publicTimelineFlow.value.copy(loadState = it)
    },
    onError = { it?.printStackTrace() },
    onRequest = { nextPage ->
      val response = api.publicTimeline(maxId = nextPage, local = true)
      if (response.isSuccessful && !response.body().isNullOrEmpty()) {
        val body = response.body()!!
        Result.success(body)
      } else Result.success(emptyList())
    },
    onSuccess = { state, item ->
      when (state) {
        LoadState.Append -> {
          val timeline = publicTimelineFlow.value.timeline
          publicTimelineFlow.emit(
            publicTimelineFlow.value.copy(
              timeline = timeline + item.toUiData(),
              endReached = item.isEmpty()
            )
          )
        }
        LoadState.Refresh -> {
          publicTimelineFlow.emit(
            publicTimelineFlow.value.copy(timeline = item.toUiData(), endReached = item.isEmpty())
          )
        }
        else -> Unit
      }
    }
  )

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

  init {
    viewModelScope.launch {
      activityAccountFlow.collect {
        uiState = uiState.copy(
          trendingNews = exploreRepository.getNews().getOrDefault(emptyList())
        )
        trendingPaginator.refresh()
        publicTimelinePaginator.refresh()
      }
    }
  }

  fun refreshExploreKind(kind: ExplorerKind) {
    viewModelScope.launch {
      when (kind) {
        ExplorerKind.Trending -> trendingPaginator.refresh()
        ExplorerKind.PublicTimeline -> publicTimelinePaginator.refresh()
        ExplorerKind.News -> TODO()
      }
    }
  }

  fun appendExploreKind(kind: ExplorerKind) {
    viewModelScope.launch {
      when (kind) {
        ExplorerKind.Trending -> trendingPaginator.append()
        ExplorerKind.PublicTimeline -> publicTimelinePaginator.append()
        ExplorerKind.News -> TODO()
      }
    }
  }

  fun onValueChange(text: String) {
    uiState = uiState.copy(text = text)
  }

  fun clearInputText() {
    uiState = uiState.copy(text = "")
  }

  fun onStatusAction(action: StatusAction, context: Context, kind: ExplorerKind, status: Status) {
    viewModelScope.launch(Dispatchers.IO) {
      when (kind) {
        ExplorerKind.Trending -> trendingFlow.update {
          it.copy(timeline = updateStatusListActions(it.timeline, action, status.id))
        }
        ExplorerKind.PublicTimeline -> publicTimelineFlow.update {
          it.copy(timeline = updateStatusListActions(it.timeline, action, status.id))
        }
        else -> Unit
      }
      statusActionHandler.onStatusAction(action, context)
    }
  }

  fun updateStatusFromDetailScreen(newStatus: StatusBackResult) {
    val trending = trendingFlow.value.timeline
    val publicTimeline = publicTimelineFlow.value.timeline
    trendingFlow.value = trendingFlow.value.copy(
      timeline = trending.updateStatusActionData(newStatus)
    )
    publicTimelineFlow.value = publicTimelineFlow.value.copy(
      timeline = publicTimeline.updateStatusActionData(newStatus)
    )
  }

  fun syncExploreKind(page: Int) {
    currentExploreKindFlow.value = ExplorerKind.entries.toTypedArray()[page]
  }

  companion object {
    const val EXPLOREPAGINGFETCHNUMBER = 20
    const val PAGINGTHRESHOLD = 5
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
