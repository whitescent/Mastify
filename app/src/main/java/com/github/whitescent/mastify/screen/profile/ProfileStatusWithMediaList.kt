package com.github.whitescent.mastify.screen.profile

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.github.whitescent.mastify.data.model.ui.StatusUiData
import com.github.whitescent.mastify.data.model.ui.StatusUiData.ReplyChainType.Null
import com.github.whitescent.mastify.network.model.account.Account
import com.github.whitescent.mastify.network.model.status.Status
import com.github.whitescent.mastify.ui.component.AppHorizontalDivider
import com.github.whitescent.mastify.ui.component.StatusEndIndicator
import com.github.whitescent.mastify.ui.component.status.StatusListItem
import com.github.whitescent.mastify.ui.component.status.paging.EmptyStatusListPlaceholder
import com.github.whitescent.mastify.ui.component.status.paging.PageType
import com.github.whitescent.mastify.ui.component.status.paging.StatusListLoadError
import com.github.whitescent.mastify.ui.component.status.paging.StatusListLoading
import com.github.whitescent.mastify.utils.StatusAction
import kotlinx.collections.immutable.ImmutableList

@Composable
fun ProfileStatusWithMediaList(
  statusList: LazyPagingItems<StatusUiData>,
  statusListWithMediaState: LazyListState,
  action: (StatusAction) -> Unit,
  navigateToDetail: (Status) -> Unit,
  navigateToProfile: (Account) -> Unit,
  navigateToMedia: (ImmutableList<Status.Attachment>, Int) -> Unit,
) {
  when (statusList.itemCount) {
    0 -> {
      when (statusList.loadState.refresh) {
        is LoadState.Error -> StatusListLoadError { statusList.refresh() }
        is LoadState.NotLoading ->
          EmptyStatusListPlaceholder(PageType.Profile, alignment = Alignment.TopCenter)
        else -> StatusListLoading()
      }
    }
    else -> {
      LazyColumn(
        state = statusListWithMediaState,
        modifier = Modifier.padding(bottom = 56.dp),
      ) {
        items(
          count = statusList.itemCount,
          contentType = statusList.itemContentType(),
          key = statusList.itemKey(),
        ) { index ->
          statusList[index]?.let {
            StatusListItem(
              status = it,
              action = action,
              replyChainType = Null,
              hasUnloadedParent = false,
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
