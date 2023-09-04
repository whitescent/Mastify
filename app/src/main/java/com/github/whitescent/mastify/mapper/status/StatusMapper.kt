package com.github.whitescent.mastify.mapper.status

import com.github.whitescent.mastify.data.model.ui.StatusUiData
import com.github.whitescent.mastify.database.model.TimelineEntity
import com.github.whitescent.mastify.network.model.status.Status
import com.github.whitescent.mastify.ui.component.generateHtmlContentWithEmoji
import kotlinx.collections.immutable.toImmutableList
import org.jsoup.Jsoup

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
    fullname = reblog?.account?.fullname ?: account.fullname,
    createdAt = reblog?.createdAt ?: createdAt,
    accountEmojis = (reblog?.account?.emojis ?: account.emojis).toImmutableList(),
    emojis = (reblog?.emojis ?: emojis).toImmutableList(),
    displayName = generateHtmlContentWithEmoji(
      reblog?.account?.realDisplayName ?: account.realDisplayName,
      reblog?.account?.emojis ?: account.emojis
    ),
    reblogDisplayName = generateHtmlContentWithEmoji(account.realDisplayName, account.emojis),
    content = Jsoup.parse(generateHtmlContentWithEmoji(
      content = reblog?.content ?: content,
      emojis = reblog?.emojis ?: emojis
    )).body().html(),
    sensitive = reblog?.sensitive ?: sensitive,
    spoilerText = reblog?.spoilerText ?: spoilerText,
    attachments = reblog?.attachments?.toImmutableList() ?: attachments.toImmutableList(),
    repliesCount = reblog?.repliesCount ?: repliesCount,
    reblogsCount = reblog?.reblogsCount ?: reblogsCount,
    favouritesCount = reblog?.favouritesCount ?: favouritesCount,
    favourited = reblog?.favourited ?: favourited,
    inReplyToId = reblog?.inReplyToId ?: inReplyToId,
    actionable = actionableStatus,
    actionableId = actionableStatus.id
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
    favourited = favourited,
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
    attachments = attachments
  )
}

fun List<Status>.toUiData() = this.map { it.toUiData() }
