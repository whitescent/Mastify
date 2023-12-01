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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.whitescent.mastify.data.model.ui.StatusUiData
import com.github.whitescent.mastify.network.model.account.Account
import com.github.whitescent.mastify.network.model.status.Status
import com.github.whitescent.mastify.network.model.trends.News
import com.github.whitescent.mastify.ui.component.HeightSpacer
import com.github.whitescent.mastify.ui.component.status.StatusCommonList
import com.github.whitescent.mastify.ui.component.status.paging.StatusListLoading
import com.github.whitescent.mastify.utils.StatusAction
import com.github.whitescent.mastify.viewModel.ExplorerKind
import com.github.whitescent.mastify.viewModel.ExplorerKind.PublicTimeline
import com.github.whitescent.mastify.viewModel.ExplorerKind.Trending
import com.github.whitescent.mastify.viewModel.StatusCommonListData
import kotlinx.collections.immutable.ImmutableList

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExplorePager(
  state: PagerState,
  trendingStatusListState: LazyListState,
  trendingStatusList: StatusCommonListData<StatusUiData>,
  publicTimelineListState: LazyListState,
  publicTimelineList: StatusCommonListData<StatusUiData>,
  newsListState: LazyListState,
  newsList: List<News>?,
  modifier: Modifier = Modifier,
  action: (StatusAction, ExplorerKind, Status) -> Unit,
  refreshKind: (ExplorerKind) -> Unit,
  append: (ExplorerKind) -> Unit,
  navigateToDetail: (Status) -> Unit,
  navigateToProfile: (Account) -> Unit,
  navigateToMedia: (ImmutableList<Status.Attachment>, Int) -> Unit,
) {
  HorizontalPager(
    state = state,
    pageContent = {
      when (it) {
        0 -> StatusCommonList(
          statusCommonListData = trendingStatusList,
          statusListState = trendingStatusListState,
          action = { action, status -> action(action, Trending, status) },
          enablePullRefresh = true,
          refreshList = { refreshKind(Trending) },
          append = { append(Trending) },
          navigateToDetail = navigateToDetail,
          navigateToProfile = navigateToProfile,
          navigateToMedia = navigateToMedia,
        )
        1 -> StatusCommonList(
          statusCommonListData = publicTimelineList,
          statusListState = publicTimelineListState,
          action = { action, status -> action(action, PublicTimeline, status) },
          enablePullRefresh = true,
          refreshList = { refreshKind(PublicTimeline) },
          append = { append(PublicTimeline) },
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
}
