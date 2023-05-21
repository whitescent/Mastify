package com.github.whitescent.mastify.ui.component.status

import androidx.compose.runtime.Composable
import com.github.whitescent.mastify.network.model.response.account.Status
import com.github.whitescent.mastify.utils.FormatFactory

@Composable
fun StatusListItem(
  status: Status
) {
  when (status.reblog) {
    null -> {
      StatusContent(
        reblogStatus = ReblogStatus(false, status.account.avatar, status.account.displayName),
        avatar = status.account.avatar,
        displayName = status.account.displayName,
        username = status.account.username,
        instanceName = FormatFactory.getInstanceName(status.account.url),
        createdAt = status.createdAt,
        content = status.content,
        application = status.application,
        sensitive = status.sensitive,
        spoilerText = status.spoilerText,
        mentions = status.mentions,
        tags = status.tags,
        mediaAttachments = status.mediaAttachments,
        repliesCount = status.repliesCount,
        reblogsCount = status.reblogsCount,
        favouritesCount = status.favouritesCount
      )
    }
    else -> {
      StatusContent(
        reblogStatus = ReblogStatus(true, status.account.avatar, status.account.displayName),
        avatar = status.reblog.account.avatar,
        displayName = status.reblog.account.displayName,
        username = status.reblog.account.username,
        instanceName = FormatFactory.getInstanceName(status.reblog.account.url),
        createdAt = status.reblog.createdAt,
        content = status.reblog.content,
        application = status.reblog.application,
        sensitive = status.sensitive,
        mentions = status.mentions,
        tags = status.tags,
        spoilerText = status.spoilerText,
        mediaAttachments = status.reblog.mediaAttachments,
        repliesCount = status.reblog.repliesCount,
        reblogsCount = status.reblog.reblogsCount,
        favouritesCount = status.reblog.favouritesCount
      )
    }
  }
}

data class ReblogStatus(
  val reblog: Boolean,
  val originalAccountAvatar: String,
  val originalAccountName: String,
)
