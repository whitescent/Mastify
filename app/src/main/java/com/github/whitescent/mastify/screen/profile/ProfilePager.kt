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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.paging.compose.LazyPagingItems
import com.github.whitescent.mastify.data.model.ui.StatusUiData
import com.github.whitescent.mastify.network.model.account.Account
import com.github.whitescent.mastify.network.model.status.Status
import com.github.whitescent.mastify.utils.StatusAction
import kotlinx.collections.immutable.ImmutableList

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProfilePager(
  state: PagerState,
  statusListState: LazyListState,
  statusWithReplyListState: LazyListState,
  statusWithMediaListState: LazyListState,
  statusList: LazyPagingItems<StatusUiData>,
  statusWithReplyList: LazyPagingItems<StatusUiData>,
  statusWithMediaList: LazyPagingItems<StatusUiData>,
  action: (StatusAction) -> Unit,
  navigateToDetail: (Status) -> Unit,
  navigateToProfile: (Account) -> Unit,
  navigateToMedia: (ImmutableList<Status.Attachment>, Int) -> Unit,
) {
  HorizontalPager(
    state = state,
    pageContent = {
      when (it) {
        0 -> ProfileStatusList(
          statusList = statusList,
          statusListState = statusListState,
          action = action,
          navigateToDetail = navigateToDetail,
          navigateToProfile = navigateToProfile,
          navigateToMedia = navigateToMedia,
        )
        1 -> ProfileStatusWithReplyList(
          statusList = statusWithReplyList,
          statusListWithReplyState = statusWithReplyListState,
          action = action,
          navigateToDetail = navigateToDetail,
          navigateToProfile = navigateToProfile,
          navigateToMedia = navigateToMedia,
        )
        2 -> ProfileStatusWithMediaList(
          statusList = statusWithMediaList,
          statusListWithMediaState = statusWithMediaListState,
          action = action,
          navigateToDetail = navigateToDetail,
          navigateToProfile = navigateToProfile,
          navigateToMedia = navigateToMedia,
        )
      }
    }
  )
}
