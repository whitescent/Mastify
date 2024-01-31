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

package com.github.whitescent.mastify.ui.component.status

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
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
import com.github.whitescent.mastify.data.model.ui.StatusUiData
import com.github.whitescent.mastify.data.model.ui.StatusUiData.ReplyChainType.End
import com.github.whitescent.mastify.data.model.ui.StatusUiData.ReplyChainType.Null
import com.github.whitescent.mastify.extensions.getReplyChainType
import com.github.whitescent.mastify.extensions.hasUnloadedParent
import com.github.whitescent.mastify.network.model.account.Account
import com.github.whitescent.mastify.network.model.status.Status
import com.github.whitescent.mastify.network.model.status.Status.Attachment
import com.github.whitescent.mastify.paging.PageLoadState
import com.github.whitescent.mastify.paging.PageLoadState.Error
import com.github.whitescent.mastify.paging.PageLoadState.NotLoading
import com.github.whitescent.mastify.paging.PageLoadState.Refresh
import com.github.whitescent.mastify.paging.Paginator
import com.github.whitescent.mastify.paging.PaginatorUiState
import com.github.whitescent.mastify.paging.rememberPaginatorUiState
import com.github.whitescent.mastify.ui.component.AppHorizontalDivider
import com.github.whitescent.mastify.ui.component.StatusAppendingIndicator
import com.github.whitescent.mastify.ui.component.StatusEndIndicator
import com.github.whitescent.mastify.ui.component.drawVerticalScrollbar
import com.github.whitescent.mastify.ui.component.status.paging.EmptyStatusListPlaceholder
import com.github.whitescent.mastify.ui.component.status.paging.PagePlaceholderType
import com.github.whitescent.mastify.ui.component.status.paging.StatusListLoadError
import com.github.whitescent.mastify.ui.component.status.paging.StatusListLoading
import com.github.whitescent.mastify.utils.StatusAction
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun LazyTimelinePagingList(
  statusListState: LazyListState,
  paginator: Paginator,
  pagingList: ImmutableList<StatusUiData>,
  modifier: Modifier = Modifier,
  contentPadding: PaddingValues = PaddingValues(bottom = 100.dp),
  paginatorUiState: PaginatorUiState = rememberPaginatorUiState(paginator),
  pagePlaceholderType: PagePlaceholderType,
  enablePullRefresh: Boolean = false,
  action: (StatusAction, Status) -> Unit,
  navigateToDetail: (Status) -> Unit,
  navigateToProfile: (Account) -> Unit,
  navigateToMedia: (ImmutableList<Attachment>, Int) -> Unit,
) {
  val context = LocalContext.current
  var refreshing by remember { mutableStateOf(false) }
  val scope = rememberCoroutineScope()
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
      }
  ) {
    when (pagingList.size) {
      0 -> {
        when {
          paginatorUiState.loadState is Error ->
            StatusListLoadError {
              scope.launch {
                paginator.refresh()
              }
            }
          paginatorUiState.loadState is NotLoading && paginatorUiState.loadState.endReached ->
            EmptyStatusListPlaceholder(
              pagePlaceholderType = pagePlaceholderType,
              modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            )
          paginatorUiState.loadState is Refresh ->
            StatusListLoading(Modifier.fillMaxSize())
        }
      }
      else -> {
        val firstVisibleItemIndex by remember(statusListState) {
          derivedStateOf {
            statusListState.firstVisibleItemIndex
          }
        }
        LazyColumn(
          state = statusListState,
          modifier = modifier.fillMaxSize().drawVerticalScrollbar(statusListState),
          contentPadding = contentPadding
        ) {
          itemsIndexed(
            items = pagingList,
            contentType = { _, _ -> StatusUiData },
            key = { _, item -> item.id }
          ) { index, status ->
            val replyChainType by remember(status, pagingList.size, index) {
              mutableStateOf(pagingList.getReplyChainType(index))
            }
            val hasUnloadedParent by remember(status, pagingList.size, index) {
              mutableStateOf(pagingList.hasUnloadedParent(index))
            }
            StatusListItem(
              status = status,
              action = {
                action(it, status.actionable)
              },
              replyChainType = replyChainType,
              hasUnloadedParent = hasUnloadedParent,
              navigateToDetail = {
                navigateToDetail(status.actionable)
              },
              navigateToProfile = navigateToProfile,
              navigateToMedia = navigateToMedia,
            )
            if (!status.hasUnloadedStatus && (replyChainType == End || replyChainType == Null))
              AppHorizontalDivider()
          }
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
        PullRefreshIndicator(refreshing, pullRefreshState, Modifier.align(Alignment.TopCenter))
        if (paginatorUiState.canPaging && pagingList.size > 0 &&
          firstVisibleItemIndex >= (pagingList.size - (pagingList.size / paginator.pageSize) * 10)
        ) {
          scope.launch(SupervisorJob()) {
            paginator.append()
          }
        }
      }
    }
  }
}
