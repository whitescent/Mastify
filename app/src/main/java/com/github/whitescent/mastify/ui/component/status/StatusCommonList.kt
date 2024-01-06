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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.github.whitescent.mastify.data.model.ui.StatusCommonListData
import com.github.whitescent.mastify.data.model.ui.StatusUiData
import com.github.whitescent.mastify.data.model.ui.StatusUiData.ReplyChainType.End
import com.github.whitescent.mastify.data.model.ui.StatusUiData.ReplyChainType.Null
import com.github.whitescent.mastify.mapper.status.getReplyChainType
import com.github.whitescent.mastify.mapper.status.hasUnloadedParent
import com.github.whitescent.mastify.network.model.account.Account
import com.github.whitescent.mastify.network.model.status.Status
import com.github.whitescent.mastify.network.model.status.Status.Attachment
import com.github.whitescent.mastify.paging.LoadState.Append
import com.github.whitescent.mastify.paging.LoadState.Error
import com.github.whitescent.mastify.paging.LoadState.NotLoading
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun StatusCommonList(
  statusCommonListData: StatusCommonListData<StatusUiData>,
  statusListState: LazyListState,
  pagePlaceholderType: PagePlaceholderType,
  modifier: Modifier = Modifier,
  enablePullRefresh: Boolean = false,
  action: (StatusAction, Status) -> Unit,
  refreshList: () -> Unit,
  append: () -> Unit,
  navigateToDetail: (Status) -> Unit,
  navigateToProfile: (Account) -> Unit,
  navigateToMedia: (ImmutableList<Attachment>, Int) -> Unit,
) {
  val statusList by remember(statusCommonListData.timeline) {
    mutableStateOf(statusCommonListData.timeline)
  }
  val loadState by remember(statusCommonListData.loadState) {
    mutableStateOf(statusCommonListData.loadState)
  }

  val context = LocalContext.current
  var refreshing by remember { mutableStateOf(false) }
  val scope = rememberCoroutineScope()
  val pullRefreshState = rememberPullRefreshState(
    refreshing = refreshing,
    onRefresh = {
      scope.launch {
        refreshing = true
        delay(500)
        refreshList()
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
    when (statusList.size) {
      0 -> {
        when {
          loadState == Error -> StatusListLoadError { refreshList() }
          loadState == NotLoading && statusCommonListData.endReached ->
            EmptyStatusListPlaceholder(
              pagePlaceholderType = pagePlaceholderType,
              modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            )
          else -> StatusListLoading(Modifier.fillMaxSize())
        }
      }
      else -> {
        LazyColumn(
          state = statusListState,
          modifier = modifier.fillMaxSize().drawVerticalScrollbar(statusListState),
          contentPadding = PaddingValues(bottom = 100.dp)
        ) {
          itemsIndexed(
            items = statusCommonListData.timeline,
            contentType = { _, _ -> StatusUiData },
            key = { _, item -> item.id }
          ) { index, status ->
            val replyChainType by remember(status, statusList.size, index) {
              mutableStateOf(statusList.getReplyChainType(index))
            }
            val hasUnloadedParent by remember(status, statusList.size, index) {
              mutableStateOf(statusList.hasUnloadedParent(index))
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
            when (loadState) {
              Append -> StatusAppendingIndicator()
              Error -> {
                // TODO Localization
                Toast.makeText(context, "获取嘟文失败，请稍后重试", Toast.LENGTH_SHORT).show()
                append() // retry
              }
              else -> Unit
            }
            if (statusCommonListData.endReached) StatusEndIndicator(Modifier.padding(36.dp))
          }
        }
        PullRefreshIndicator(refreshing, pullRefreshState, Modifier.align(Alignment.TopCenter))
      }
    }
  }
}
