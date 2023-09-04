package com.github.whitescent.mastify.screen.profile

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.github.whitescent.mastify.data.model.ui.StatusUiData
import com.github.whitescent.mastify.data.model.ui.StatusUiData.ReplyChainType.Null
import com.github.whitescent.mastify.ui.component.AppHorizontalDivider
import com.github.whitescent.mastify.ui.component.StatusEndIndicator
import com.github.whitescent.mastify.ui.component.status.StatusListItem

@Composable
fun ProfileStatus(
  accountStatus: LazyPagingItems<StatusUiData>
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize().padding(bottom = 56.dp),
  ) {
    items(
      count = accountStatus.itemCount,
      contentType = accountStatus.itemContentType(),
      key = accountStatus.itemKey(),
    ) {
      accountStatus[it]?.let { status ->
        StatusListItem(
          status = status,
          menuAction = { },
          replyChainType = Null,
          hasUnloadedParent = false,
          favouriteStatus = { /*TODO*/ },
          unfavouriteStatus = { /*TODO*/ },
          navigateToDetail = { /*TODO*/ },
          navigateToProfile = { },
          navigateToMedia = { index, target -> },
          modifier = Modifier.padding(horizontal = 8.dp)
        )
      }
      AppHorizontalDivider()
    }
    item {
      StatusEndIndicator(Modifier.padding(54.dp))
    }
  }
}
