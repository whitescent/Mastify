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

package com.github.whitescent.mastify.paging

import android.widget.Toast
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastMap
import com.github.whitescent.mastify.paging.PageLoadState.Error
import com.github.whitescent.mastify.paging.PageLoadState.NotLoading
import com.github.whitescent.mastify.paging.PageLoadState.Refresh
import com.github.whitescent.mastify.ui.component.StatusAppendingIndicator
import com.github.whitescent.mastify.ui.component.StatusEndIndicator
import com.github.whitescent.mastify.ui.component.drawVerticalScrollbar
import com.github.whitescent.mastify.ui.component.status.paging.EmptyStatusListPlaceholder
import com.github.whitescent.mastify.ui.component.status.paging.PagePlaceholderType
import com.github.whitescent.mastify.ui.component.status.paging.StatusListLoadError
import com.github.whitescent.mastify.ui.component.status.paging.StatusListLoading
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun <T : Any> LazyPagingList(
  paginator: Paginator,
  modifier: Modifier = Modifier,
  paginatorUiState: PaginatorUiState = rememberPaginatorUiState(paginator),
  list: ImmutableList<T>,
  showScrollbar: Boolean = true,
  lazyListState: LazyListState = rememberLazyListState(),
  contentPadding: PaddingValues = PaddingValues(0.dp),
  pagePlaceholderType: PagePlaceholderType,
  reverseLayout: Boolean = false,
  enablePullRefresh: Boolean = false,
  verticalArrangement: Arrangement.Vertical =
    if (!reverseLayout) Arrangement.Top else Arrangement.Bottom,
  horizontalAlignment: Alignment.Horizontal = Alignment.Start,
  flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
  userScrollEnabled: Boolean = true,
  placeholder: @Composable (() -> Unit)? = null,
  footer: (LazyListScope.() -> Unit)? = null,
  content: LazyListScope.() -> Unit,
) {
  val scope = rememberCoroutineScope()
  val context = LocalContext.current
  var refreshing by remember { mutableStateOf(false) }
  val pullRefreshState = rememberPullRefreshState(
    refreshing = refreshing,
    onRefresh = {
      scope.launch {
        refreshing = true
        delay(500)
        paginator.refresh()
        refreshing = false
      }
    }
  )
  Box(
    modifier = Modifier
      .fillMaxSize()
      .let {
        if (enablePullRefresh) it.pullRefresh(pullRefreshState) else it
      },
  ) {
    when (list.size) {
      0 -> {
        if (placeholder == null) {
          when {
            paginatorUiState.loadState is Error ->
              StatusListLoadError(paginatorUiState.loadState.throwable.localizedMessage) {
                scope.launch(SupervisorJob()) {
                  paginator.refresh()
                }
              }
            paginatorUiState.loadState is NotLoading && paginatorUiState.endReached -> {
              EmptyStatusListPlaceholder(
                pagePlaceholderType = pagePlaceholderType,
                modifier = Modifier.fillMaxSize()
                  .verticalScroll(rememberScrollState())
                  .pullRefresh(pullRefreshState),
              )
            }
            paginatorUiState.loadState is Refresh -> StatusListLoading(Modifier.fillMaxSize())
          }
        } else placeholder()
      }
      else -> {
        val visibleItemsIndex by remember(lazyListState) {
          derivedStateOf {
            lazyListState.layoutInfo.visibleItemsInfo.fastMap { it.index }
          }
        }
        LazyColumn(
          state = lazyListState,
          modifier = modifier
            .let {
              if (showScrollbar) it.drawVerticalScrollbar(lazyListState) else it
            },
          contentPadding = contentPadding,
          reverseLayout = reverseLayout,
          verticalArrangement = verticalArrangement,
          horizontalAlignment = horizontalAlignment,
          flingBehavior = flingBehavior,
          userScrollEnabled = userScrollEnabled,
        ) {
          content()
          if (footer != null) footer()
          else {
            item {
              when (paginatorUiState.loadState) {
                is PageLoadState.Append -> StatusAppendingIndicator()
                is Error -> {
                  // TODO Localization
                  Toast.makeText(
                    context,
                    paginatorUiState.loadState.throwable.localizedMessage,
                    Toast.LENGTH_SHORT
                  ).show()
                }
                else -> Unit
              }
              if (paginatorUiState.endReached) StatusEndIndicator(Modifier.padding(36.dp))
            }
          }
        }
        if (
          paginatorUiState.canPaging && list.size > 0 && visibleItemsIndex.contains(list.size - 6)
        ) {
          scope.launch(SupervisorJob()) {
            paginator.append()
          }
        }
      }
    }
    PullRefreshIndicator(refreshing, pullRefreshState, Modifier.align(Alignment.TopCenter))
  }
}
