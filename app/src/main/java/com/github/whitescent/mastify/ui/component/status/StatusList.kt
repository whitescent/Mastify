package com.github.whitescent.mastify.ui.component.status

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.github.whitescent.mastify.AppTheme
import com.github.whitescent.mastify.network.model.response.account.Status
import com.github.whitescent.mastify.ui.component.CenterRow
import com.github.whitescent.mastify.ui.component.WidthSpacer
import com.github.whitescent.mastify.utils.FormatFactory

@Composable
fun StatusList(
  statuses: List<Status>,
  modifier: Modifier = Modifier
) {
  LazyColumn(
    modifier = modifier.fillMaxSize()
  ) {
    itemsIndexed(statuses) { _, status ->
      if (status.content.isNotEmpty() || status.mediaAttachments.isNotEmpty() ||
        status.reblog != null
      ) {
        StatusListItem(status)
      }
      Divider(modifier = Modifier.fillMaxWidth(), thickness = (0.6).dp)
    }
  }
}

@Composable
fun StatusListItem(
  status: Status
) {
  Column(
    modifier = Modifier.fillMaxWidth()
  ) {
    status.reblog?.let {
      CenterRow(
        modifier = Modifier.padding(start = 24.dp, top = 8.dp)
      ) {
        Icon(
          imageVector = Icons.Rounded.Repeat,
          contentDescription = null,
          tint = Color.Gray,
          modifier = Modifier.size(22.dp)
        )
        WidthSpacer(value = 4.dp)
        Text(
          text = "${status.account.displayName} 转了这篇嘟文",
          style = AppTheme.typography.titleSmall,
          color = AppTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
      }
    }
    when (status.reblog) {
      null -> {
        StatusContent(
          avatar = status.account.avatar,
          displayName = status.account.displayName,
          username = status.account.username,
          instanceName = FormatFactory.getInstanceName(status.account.url),
          createdAt = status.createdAt,
          content = status.content,
          mediaAttachments = status.mediaAttachments,
          repliesCount = status.repliesCount,
          reblogsCount = status.reblogsCount,
          favouritesCount = status.favouritesCount
        )
      }
      else -> {
        StatusContent(
          avatar = status.reblog.account.avatar,
          displayName = status.reblog.account.displayName,
          username = status.reblog.account.username,
          instanceName = FormatFactory.getInstanceName(status.reblog.account.url),
          createdAt = status.reblog.createdAt,
          content = status.reblog.content,
          mediaAttachments = status.reblog.mediaAttachments,
          repliesCount = status.reblog.repliesCount,
          reblogsCount = status.reblog.reblogsCount,
          favouritesCount = status.reblog.favouritesCount
        )
      }
    }
    Divider(thickness = 0.6.dp)
  }
}
