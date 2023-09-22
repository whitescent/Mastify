package com.github.whitescent.mastify.mapper.status

import androidx.paging.compose.LazyPagingItems
import com.github.whitescent.mastify.data.model.ui.StatusUiData
import com.github.whitescent.mastify.data.model.ui.StatusUiData.ReplyChainType
import com.github.whitescent.mastify.data.model.ui.StatusUiData.ReplyChainType.Continue
import com.github.whitescent.mastify.data.model.ui.StatusUiData.ReplyChainType.End
import com.github.whitescent.mastify.data.model.ui.StatusUiData.ReplyChainType.Null
import com.github.whitescent.mastify.data.model.ui.StatusUiData.ReplyChainType.Start
import com.github.whitescent.mastify.data.model.ui.StatusUiData.Visibility.Companion.byString
import com.github.whitescent.mastify.database.model.TimelineEntity
import com.github.whitescent.mastify.network.model.status.Status
import com.github.whitescent.mastify.ui.component.generateHtmlContentWithEmoji
import kotlinx.collections.immutable.toImmutableList

fun Status.toUiData(): StatusUiData {
  return StatusUiData(
    id = id,
    reblog = reblog,
    account = account,
    link = actionableStatus.uri,
    accountId = account.id,
    avatar = reblog?.account?.avatar ?: account.avatar,
    application = reblog?.application ?: application,
    reblogged = reblog?.reblogged ?: reblogged,
    bookmarked = reblog?.bookmarked ?: bookmarked,
    rebloggedAvatar = account.avatar,
    visibility = byString(visibility),
    fullname = reblog?.account?.fullname ?: account.fullname,
    createdAt = reblog?.createdAt ?: createdAt,
    accountEmojis = (reblog?.account?.emojis ?: account.emojis).toImmutableList(),
    emojis = (reblog?.emojis ?: emojis).toImmutableList(),
    displayName = generateHtmlContentWithEmoji(
      reblog?.account?.realDisplayName ?: account.realDisplayName,
      reblog?.account?.emojis ?: account.emojis
    ),
    reblogDisplayName = generateHtmlContentWithEmoji(account.realDisplayName, account.emojis),
    content = generateHtmlContentWithEmoji(
      content = reblog?.content ?: content,
      emojis = reblog?.emojis ?: emojis
    ),
    sensitive = reblog?.sensitive ?: sensitive,
    spoilerText = reblog?.spoilerText ?: spoilerText,
    attachments = reblog?.attachments?.toImmutableList() ?: attachments.toImmutableList(),
    repliesCount = reblog?.repliesCount ?: repliesCount,
    reblogsCount = reblog?.reblogsCount ?: reblogsCount,
    favouritesCount = reblog?.favouritesCount ?: favouritesCount,
    favorited = reblog?.favorited ?: favorited,
    inReplyToId = reblog?.inReplyToId ?: inReplyToId,
    actionable = actionableStatus,
    actionableId = actionableStatus.id,
    hasUnloadedStatus = hasUnloadedStatus
  )
}

fun List<Status>.toEntity(timelineUserId: Long): List<TimelineEntity> {
  return this.map { it.toEntity(timelineUserId) }
}

fun Status.toEntity(timelineUserId: Long): TimelineEntity {
  return TimelineEntity(
    id = id,
    timelineUserId = timelineUserId,
    createdAt = createdAt,
    sensitive = sensitive,
    spoilerText = spoilerText,
    visibility = visibility,
    uri = uri,
    url = url,
    repliesCount = repliesCount,
    reblogsCount = reblogsCount,
    favouritesCount = favouritesCount,
    editedAt = editedAt,
    favorited = favorited,
    inReplyToId = inReplyToId,
    inReplyToAccountId = inReplyToAccountId,
    reblogged = reblogged,
    bookmarked = bookmarked,
    reblog = reblog,
    content = content,
    emojis = emojis,
    tags = tags,
    mentions = mentions,
    account = account,
    application = application,
    attachments = attachments,
    hasUnloadedStatus = hasUnloadedStatus
  )
}

fun List<Status>.toUiData() = this.map { it.toUiData() }

fun LazyPagingItems<StatusUiData>.hasUnloadedParent(index: Int): Boolean {
  val current = this[index] ?: return false
  val currentType = getReplyChainType(index)
  if (currentType == Null || !current.isInReplyTo) return false
  return when (val prev = peekOrNull(index - 1)) {
    null -> false
    else -> current.inReplyToId != prev.id
  }
}

fun LazyPagingItems<StatusUiData>.getReplyChainType(index: Int): ReplyChainType {
  val prev = peekOrNull(index - 1)
  val current = peekOrNull(index) ?: return Null
  val next = peekOrNull(index + 1)

  return when {
    prev != null && next != null -> {
      when {
        (current.isInReplyTo &&
          current.inReplyToId == prev.id && next.inReplyToId == current.id) -> Continue
        next.inReplyToId == current.id -> Start
        current.inReplyToId == prev.id -> End
        else -> Null
      }
    }
    prev == null && next != null -> {
      when (next.inReplyToId) {
        current.id -> Start
        else -> Null
      }
    }
    prev != null && next == null -> {
      when {
        current.isInReplyTo && current.inReplyToId == prev.id -> End
        else -> Null
      }
    }
    else -> Null
  }
}

fun List<StatusUiData>.hasUnloadedParent(index: Int): Boolean {
  val current = get(index)
  val currentType = getReplyChainType(index)
  if (currentType == Null || !current.isInReplyTo) return false
  return when (val prev = getOrNull(index - 1)) {
    null -> false
    else -> current.inReplyToId != prev.id
  }
}

fun List<StatusUiData>.getReplyChainType(index: Int): ReplyChainType {
  val prev = getOrNull(index - 1)
  val current = get(index)
  val next = getOrNull(index + 1)

  return when {
    prev != null && next != null -> {
      when {
        (current.isInReplyTo &&
          current.inReplyToId == prev.id && next.inReplyToId == current.id) -> Continue
        next.inReplyToId == current.id -> Start
        current.inReplyToId == prev.id -> End
        else -> Null
      }
    }
    prev == null && next != null -> {
      when (next.inReplyToId) {
        current.id -> Start
        else -> Null
      }
    }
    prev != null && next == null -> {
      when {
        current.isInReplyTo && current.inReplyToId == prev.id -> End
        else -> Null
      }
    }
    else -> Null
  }
}

fun <T : Any> LazyPagingItems<T>.peekOrNull(index: Int): T? {
  return if (index in 0 until itemCount) peek(index) else null
}
