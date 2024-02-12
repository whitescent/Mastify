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

package com.github.whitescent.mastify.screen.explore

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.whitescent.mastify.mapper.toUiData
import com.github.whitescent.mastify.network.model.account.Account
import com.github.whitescent.mastify.network.model.status.Status
import com.github.whitescent.mastify.paging.LazyPagingList
import com.github.whitescent.mastify.ui.component.HeightSpacer
import com.github.whitescent.mastify.ui.component.status.LazyTimelinePagingList
import com.github.whitescent.mastify.ui.component.status.paging.PagePlaceholderType
import com.github.whitescent.mastify.viewModel.ExploreViewModel
import com.github.whitescent.mastify.viewModel.ExplorerKind.News
import com.github.whitescent.mastify.viewModel.ExplorerKind.PublicTimeline
import com.github.whitescent.mastify.viewModel.ExplorerKind.Trending
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Composable
fun ExplorePager(
  state: PagerState,
  trendingStatusListState: LazyListState,
  publicTimelineListState: LazyListState,
  newsListState: LazyListState,
  viewModel: ExploreViewModel,
  modifier: Modifier = Modifier,
  navigateToDetail: (Status) -> Unit,
  navigateToProfile: (Account) -> Unit,
  navigateToMedia: (ImmutableList<Status.Attachment>, Int) -> Unit,
) {
  val trendingStatusList by viewModel.trending.collectAsStateWithLifecycle()
  val publicTimelineList by viewModel.publicTimeline.collectAsStateWithLifecycle()
  val newsList by viewModel.news.collectAsStateWithLifecycle()

  HorizontalPager(
    state = state,
    pageContent = { page ->
      when (page) {
        0 -> LazyTimelinePagingList(
          statusListState = trendingStatusListState,
          paginator = viewModel.trendingPaginator,
          pagingList = trendingStatusList.distinctBy { it.id }.toUiData().toImmutableList(),
          pagePlaceholderType = PagePlaceholderType.Explore(Trending),
          action = { action, status ->
            viewModel.onStatusAction(action, Trending, status)
          },
          enablePullRefresh = true,
          navigateToDetail = navigateToDetail,
          navigateToProfile = navigateToProfile,
          navigateToMedia = navigateToMedia,
        )
        1 -> LazyTimelinePagingList(
          statusListState = publicTimelineListState,
          paginator = viewModel.publicTimelinePaginator,
          pagingList = publicTimelineList.toImmutableList(),
          pagePlaceholderType = PagePlaceholderType.Explore(PublicTimeline),
          action = { action, status ->
            viewModel.onStatusAction(action, PublicTimeline, status)
          },
          enablePullRefresh = true,
          navigateToDetail = navigateToDetail,
          navigateToProfile = navigateToProfile,
          navigateToMedia = navigateToMedia,
        )
        2 -> {
          LazyPagingList(
            paginator = viewModel.newsPaginator,
            list = newsList.toImmutableList(),
            pagePlaceholderType = PagePlaceholderType.Explore(News),
            lazyListState = newsListState,
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(
              start = 16.dp,
              end = 16.dp,
              top = 12.dp,
              bottom = 150.dp
            ),
            enablePullRefresh = true
          ) {
            items(newsList) { news ->
              ExploreNewsItem(news)
              HeightSpacer(value = 8.dp)
            }
          }
        }
      }
    },
    modifier = modifier.fillMaxSize(),
  )
}
