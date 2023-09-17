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
import com.github.whitescent.mastify.viewModel.StatusAction
import kotlinx.collections.immutable.ImmutableList

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProfilePager(
  state: PagerState,
  statusListState: LazyListState,
  action: (StatusAction) -> Unit,
  accountStatus: LazyPagingItems<StatusUiData>,
  navigateToDetail: (Status) -> Unit,
  navigateToProfile: (Account) -> Unit,
  navigateToMedia: (ImmutableList<Status.Attachment>, Int) -> Unit,
) {
  HorizontalPager(
    state = state,
    pageContent = {
      when (it) {
        0 -> ProfileStatusList(
          accountStatus = accountStatus,
          statusListState = statusListState,
          action = action,
          navigateToDetail = navigateToDetail,
          navigateToProfile = navigateToProfile,
          navigateToMedia = navigateToMedia,
        )
        1 -> Unit
        2 -> Unit
      }
    }
  )
}
