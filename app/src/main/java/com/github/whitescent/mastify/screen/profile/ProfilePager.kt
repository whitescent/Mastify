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

package com.github.whitescent.mastify.screen.profile

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.whitescent.mastify.network.model.account.Account
import com.github.whitescent.mastify.network.model.status.Status
import com.github.whitescent.mastify.network.model.status.Status.Attachment
import com.github.whitescent.mastify.ui.component.status.LazyTimelinePagingList
import com.github.whitescent.mastify.ui.component.status.paging.PagePlaceholderType.Profile
import com.github.whitescent.mastify.viewModel.ProfileViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Composable
fun ProfilePager(
  state: PagerState,
  statusListState: LazyListState,
  statusListWithReplyState: LazyListState,
  statusListWithMediaState: LazyListState,
  viewModel: ProfileViewModel,
  navigateToDetail: (Status) -> Unit,
  navigateToProfile: (Account) -> Unit,
  navigateToTagInfo: (String) -> Unit,
  navigateToMedia: (ImmutableList<Attachment>, Int) -> Unit,
) {
  val statusList by viewModel.profileStatus.collectAsStateWithLifecycle()
  val statusListWithReply by viewModel.profileStatusWithReply.collectAsStateWithLifecycle()
  val statusListWithMedia by viewModel.profileStatusWithMedia.collectAsStateWithLifecycle()

  HorizontalPager(
    state = state,
    modifier = Modifier.fillMaxSize(),
    pageContent = {
      when (it) {
        0 -> LazyTimelinePagingList(
          lazyListState = statusListState,
          paginator = viewModel.statusPaginator,
          pagingList = statusList.toImmutableList(),
          pagePlaceholderType = Profile(isSelf = viewModel.uiState.isSelf!!),
          action = { action, status ->
            viewModel.onStatusAction(action, status)
          },
          navigateToDetail = navigateToDetail,
          navigateToProfile = navigateToProfile,
          navigateToTagInfo = navigateToTagInfo,
          navigateToMedia = navigateToMedia,
        )
        1 -> LazyTimelinePagingList(
          lazyListState = statusListWithReplyState,
          paginator = viewModel.statusWithReplyPaginator,
          pagingList = statusListWithReply.toImmutableList(),
          pagePlaceholderType = Profile(isSelf = viewModel.uiState.isSelf!!),
          action = { action, status ->
            viewModel.onStatusAction(action, status)
          },
          navigateToDetail = navigateToDetail,
          navigateToProfile = navigateToProfile,
          navigateToTagInfo = navigateToTagInfo,
          navigateToMedia = navigateToMedia,
        )
        2 -> LazyTimelinePagingList(
          lazyListState = statusListWithMediaState,
          paginator = viewModel.statusWithMediaPaginator,
          pagingList = statusListWithMedia.toImmutableList(),
          pagePlaceholderType = Profile(isSelf = viewModel.uiState.isSelf!!),
          action = { action, status ->
            viewModel.onStatusAction(action, status)
          },
          navigateToDetail = navigateToDetail,
          navigateToProfile = navigateToProfile,
          navigateToTagInfo = navigateToTagInfo,
          navigateToMedia = navigateToMedia,
        )
      }
    }
  )
}
