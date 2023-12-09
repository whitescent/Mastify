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

package com.github.whitescent.mastify.screen.explore

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.whitescent.mastify.network.model.account.Account
import com.github.whitescent.mastify.network.model.status.Status
import com.github.whitescent.mastify.paging.LoadState.NotLoading
import com.github.whitescent.mastify.ui.component.HeightSpacer
import com.github.whitescent.mastify.ui.component.status.StatusCommonList
import com.github.whitescent.mastify.ui.component.status.paging.StatusListLoading
import com.github.whitescent.mastify.viewModel.ExplorerKind.PublicTimeline
import com.github.whitescent.mastify.viewModel.ExplorerKind.Trending
import com.github.whitescent.mastify.viewModel.ExplorerViewModel
import com.github.whitescent.mastify.viewModel.ExplorerViewModel.Companion.EXPLOREPAGINGFETCHNUMBER
import com.github.whitescent.mastify.viewModel.ExplorerViewModel.Companion.PAGINGTHRESHOLD
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExplorePager(
  state: PagerState,
  trendingStatusListState: LazyListState,
  publicTimelineListState: LazyListState,
  newsListState: LazyListState,
  viewModel: ExplorerViewModel,
  modifier: Modifier = Modifier,
  navigateToDetail: (Status) -> Unit,
  navigateToProfile: (Account) -> Unit,
  navigateToMedia: (ImmutableList<Status.Attachment>, Int) -> Unit,
) {
  val trendingStatusList by viewModel.trending.collectAsStateWithLifecycle()
  val publicTimelineList by viewModel.publicTimeline.collectAsStateWithLifecycle()
  val newsList = viewModel.uiState.trendingNews
  val context = LocalContext.current

  HorizontalPager(
    state = state,
    pageContent = {
      when (it) {
        0 -> StatusCommonList(
          statusCommonListData = trendingStatusList,
          statusListState = trendingStatusListState,
          action = { action, status ->
            viewModel.onStatusAction(action, context, Trending, status)
          },
          enablePullRefresh = true,
          refreshList = { viewModel.refreshExploreKind(Trending) },
          append = { viewModel.appendExploreKind(Trending) },
          navigateToDetail = navigateToDetail,
          navigateToProfile = navigateToProfile,
          navigateToMedia = navigateToMedia,
        )
        1 -> StatusCommonList(
          statusCommonListData = publicTimelineList,
          statusListState = publicTimelineListState,
          action = { action, status ->
            viewModel.onStatusAction(action, context, Trending, status)
          },
          enablePullRefresh = true,
          refreshList = { viewModel.refreshExploreKind(PublicTimeline) },
          append = { viewModel.appendExploreKind(PublicTimeline) },
          navigateToDetail = navigateToDetail,
          navigateToProfile = navigateToProfile,
          navigateToMedia = navigateToMedia,
        )
        2 -> {
          when (newsList) {
            null -> StatusListLoading(Modifier.fillMaxSize())
            else -> {
              LazyColumn(
                state = newsListState,
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                  start = 16.dp,
                  end = 16.dp,
                  top = 12.dp,
                  bottom = 150.dp
                )
              ) {
                items(newsList) { news ->
                  ExploreNewsItem(news)
                  HeightSpacer(value = 8.dp)
                }
              }
            }
          }
        }
      }
    },
    modifier = modifier.fillMaxSize(),
  )
  LaunchedEffect(viewModel) {
    // TODO There is a need to encapsulate a layer of methods for the pagination's append request,
    // but I haven't thought of a suitable way to do this yet,
    // I tried wrapping it into a @Composable, but it causes LeftCompositionCancellationException
    launch {
      snapshotFlow { trendingStatusListState.firstVisibleItemIndex }
        .filter { trendingStatusList.timeline.isNotEmpty() }
        .map {
          !viewModel.trendingPaginator.endReached && viewModel.trendingPaginator.loadState == NotLoading &&
            it >= (trendingStatusList.timeline.size - ((trendingStatusList.timeline.size / EXPLOREPAGINGFETCHNUMBER) * PAGINGTHRESHOLD))
        }
        .filter { it }
        .collect {
          viewModel.trendingPaginator.append()
        }
    }
    launch {
      snapshotFlow { publicTimelineListState.firstVisibleItemIndex }
        .filter { publicTimelineList.timeline.isNotEmpty() }
        .map {
          !viewModel.publicTimelinePaginator.endReached && viewModel.publicTimelinePaginator.loadState == NotLoading &&
            it >= (publicTimelineList.timeline.size - ((publicTimelineList.timeline.size / EXPLOREPAGINGFETCHNUMBER) * PAGINGTHRESHOLD))
        }
        .filter { it }
        .collect {
          viewModel.publicTimelinePaginator.append()
        }
    }
  }
}
