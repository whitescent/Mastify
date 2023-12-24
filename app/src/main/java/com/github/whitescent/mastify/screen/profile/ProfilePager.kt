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

package com.github.whitescent.mastify.screen.profile

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.whitescent.mastify.network.model.account.Account
import com.github.whitescent.mastify.network.model.status.Status
import com.github.whitescent.mastify.network.model.status.Status.Attachment
import com.github.whitescent.mastify.paging.autoAppend
import com.github.whitescent.mastify.ui.component.status.StatusCommonList
import com.github.whitescent.mastify.viewModel.ExplorerViewModel
import com.github.whitescent.mastify.viewModel.ProfileKind
import com.github.whitescent.mastify.viewModel.ProfileViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProfilePager(
  state: PagerState,
  statusListState: LazyListState,
  statusListWithReplyState: LazyListState,
  statusListWithMediaState: LazyListState,
  viewModel: ProfileViewModel,
  navigateToDetail: (Status) -> Unit,
  navigateToProfile: (Account) -> Unit,
  navigateToMedia: (ImmutableList<Attachment>, Int) -> Unit,
) {
  val context = LocalContext.current

  val statusList by viewModel.profileStatus.collectAsStateWithLifecycle()
  val statusListWithReply by viewModel.profileStatusWithReply.collectAsStateWithLifecycle()
  val statusListWithMedia by viewModel.profileStatusWithMedia.collectAsStateWithLifecycle()

  HorizontalPager(
    state = state,
    modifier = Modifier.fillMaxSize(),
    pageContent = {
      when (it) {
        0 -> StatusCommonList(
          statusCommonListData = statusList,
          statusListState = statusListState,
          action = { action, status ->
            viewModel.onStatusAction(action, context, status)
          },
          refreshList = { viewModel.refreshProfileKind(ProfileKind.Status) },
          append = { viewModel.appendProfileKind(ProfileKind.Status) },
          navigateToDetail = navigateToDetail,
          navigateToProfile = navigateToProfile,
          navigateToMedia = navigateToMedia,
        )
        1 -> StatusCommonList(
          statusCommonListData = statusListWithReply,
          statusListState = statusListWithReplyState,
          action = { action, status ->
            viewModel.onStatusAction(action, context, status)
          },
          refreshList = { viewModel.refreshProfileKind(ProfileKind.StatusWithReply) },
          append = { viewModel.appendProfileKind(ProfileKind.StatusWithReply) },
          navigateToDetail = navigateToDetail,
          navigateToProfile = navigateToProfile,
          navigateToMedia = navigateToMedia,
        )
        2 -> StatusCommonList(
          statusCommonListData = statusListWithMedia,
          statusListState = statusListWithMediaState,
          action = { action, status ->
            viewModel.onStatusAction(action, context, status)
          },
          refreshList = { viewModel.refreshProfileKind(ProfileKind.StatusWithMedia) },
          append = { viewModel.appendProfileKind(ProfileKind.StatusWithMedia) },
          navigateToDetail = navigateToDetail,
          navigateToProfile = navigateToProfile,
          navigateToMedia = navigateToMedia
        )
      }
    }
  )

  LaunchedEffect(viewModel) {
    launch {
      autoAppend(
        paginator = viewModel.statusPager,
        currentListIndex = { statusListState.firstVisibleItemIndex },
        fetchNumber = ExplorerViewModel.EXPLOREPAGINGFETCHNUMBER,
        threshold = 8,
      ) { statusList.timeline }
    }

    launch {
      autoAppend(
        paginator = viewModel.statusWithReplyPager,
        currentListIndex = { statusListWithReplyState.firstVisibleItemIndex },
        fetchNumber = ExplorerViewModel.EXPLOREPAGINGFETCHNUMBER,
        threshold = 8,
      ) { statusListWithReply.timeline }
    }

    launch {
      autoAppend(
        paginator = viewModel.statusWithMediaPager,
        currentListIndex = { statusListWithMediaState.firstVisibleItemIndex },
        fetchNumber = ExplorerViewModel.EXPLOREPAGINGFETCHNUMBER,
        threshold = 8,
      ) { statusListWithMedia.timeline }
    }
  }
}
