package com.github.whitescent.mastify.ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.github.whitescent.mastify.data.model.ui.StatusUiData
import com.github.whitescent.mastify.data.model.ui.StatusUiData.ReplyChainType.End
import com.github.whitescent.mastify.data.model.ui.StatusUiData.ReplyChainType.Null
import com.github.whitescent.mastify.data.model.ui.getReplyChainType
import com.github.whitescent.mastify.network.model.account.Account
import com.github.whitescent.mastify.network.model.status.Status
import com.github.whitescent.mastify.ui.component.status.StatusListItem
import kotlinx.collections.immutable.ImmutableList

@OptIn(ExperimentalFoundationApi::class)
fun LazyListScope.statusComment(
  descendants: ImmutableList<StatusUiData>,
  favouriteStatus: (String) -> Unit,
  unfavouriteStatus: (String) -> Unit,
  navigateToDetail: (Status) -> Unit,
  navigateToProfile: (Account) -> Unit,
  navigateToMedia: (List<Status.Attachment>, Int) -> Unit,
) {
  when (descendants.isEmpty()) {
    true -> item {
      StatusEndIndicator(Modifier.padding(36.dp))
    }
    else -> {
      itemsIndexed(
        items = descendants,
        key = { _, item -> item.id }
      ) { index, item ->
        val replyChainType = remember(item) { descendants.getReplyChainType(index) }
        StatusListItem(
          status = item,
          replyChainType = replyChainType,
          hasUnloadedParent = false,
          favouriteStatus = { favouriteStatus(item.actionableId) },
          unfavouriteStatus = { unfavouriteStatus(item.actionableId) },
          navigateToDetail = { navigateToDetail(item.actionable) },
          navigateToMedia = navigateToMedia,
          navigateToProfile = navigateToProfile,
          modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp).animateItemPlacement(),
        )
        if (replyChainType == Null || replyChainType == End) AppHorizontalDivider()
      }
      item {
        StatusEndIndicator(Modifier.padding(36.dp))
      }
    }
  }
}

@Composable
fun StatusEndIndicator(
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier.fillMaxWidth(),
    contentAlignment = Alignment.Center
  ) {
    Box(Modifier.size(4.dp).background(Color.Gray, CircleShape))
  }
}
