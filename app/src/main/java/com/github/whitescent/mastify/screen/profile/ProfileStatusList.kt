package com.github.whitescent.mastify.screen.profile

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.github.whitescent.mastify.data.model.ui.StatusUiData
import com.github.whitescent.mastify.mapper.status.getReplyChainType
import com.github.whitescent.mastify.mapper.status.hasUnloadedParent
import com.github.whitescent.mastify.network.model.account.Account
import com.github.whitescent.mastify.network.model.status.Status
import com.github.whitescent.mastify.ui.component.AppHorizontalDivider
import com.github.whitescent.mastify.ui.component.StatusEndIndicator
import com.github.whitescent.mastify.ui.component.status.StatusListItem
import com.github.whitescent.mastify.ui.component.status.paging.EmptyStatusListPlaceholder
import com.github.whitescent.mastify.ui.component.status.paging.StatusListLoadError
import com.github.whitescent.mastify.ui.component.status.paging.StatusListLoading
import com.github.whitescent.mastify.viewModel.StatusAction
import kotlinx.collections.immutable.ImmutableList

@Composable
fun ProfileStatusList(
  statusListState: LazyListState,
  accountStatus: LazyPagingItems<StatusUiData>,
  action: (StatusAction) -> Unit,
  navigateToDetail: (Status) -> Unit,
  navigateToProfile: (Account) -> Unit,
  navigateToMedia: (ImmutableList<Status.Attachment>, Int) -> Unit,
) {
  Crossfade(targetState = accountStatus.itemCount) {
    when (it) {
      0 -> {
        when (accountStatus.loadState.refresh) {
          is LoadState.Error -> StatusListLoadError { accountStatus.refresh() }
          is LoadState.NotLoading -> EmptyStatusListPlaceholder()
          else -> StatusListLoading()
        }
      }
      else -> {
        LazyColumn(
          state = statusListState,
          modifier = Modifier.padding(bottom = 56.dp),
        ) {
          accountStatus.itemCount
          items(
            count = accountStatus.itemCount,
            contentType = accountStatus.itemContentType(),
            key = accountStatus.itemKey(),
          ) { index ->
            val status = accountStatus[index]
            val replyChainType by remember(status, accountStatus.itemCount, index) {
              mutableStateOf(accountStatus.getReplyChainType(index))
            }
            val hasUnloadedParent by remember(status, accountStatus.itemCount, index) {
              mutableStateOf(accountStatus.hasUnloadedParent(index))
            }
            accountStatus[index]?.let {
              StatusListItem(
                status = it,
                action = action,
                replyChainType = replyChainType,
                hasUnloadedParent = hasUnloadedParent,
                navigateToDetail = { navigateToDetail(it.actionable) },
                navigateToProfile = navigateToProfile,
                navigateToMedia = navigateToMedia,
                modifier = Modifier.padding(horizontal = 8.dp),
              )
            }
            AppHorizontalDivider()
          }
          item {
            StatusEndIndicator(Modifier.padding(54.dp))
          }
        }
      }
    }
  }
}
