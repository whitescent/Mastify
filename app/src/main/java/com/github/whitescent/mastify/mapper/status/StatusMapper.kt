/*
 * Copyright 2024 WhiteScent
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

package com.github.whitescent.mastify.mapper.status

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

fun Status.toUiData(): StatusUiData = StatusUiData(
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
  visibility = byString(reblog?.visibility ?: visibility),
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
  poll = reblog?.poll ?: poll,
  // NOTE: make sure that you need to do a conversion from Status to StatusUiData
  // or update StatusUiData.actionable
  // whenever you update some StatusUiData values,
  // otherwise it may cause inconsistencies between StatusUiData and StatusUiData.actionable
  // e.g:
  //  var statusUi = Status(favCount = 19).toUiData()
  //  statusUi = statusUi.copy(favCount = 20)
  //  println(statusUi.favCount) // 20
  //  println(statusUi.actionable.favCount) // 19 !!
  //
  //  var statusUi = Status(favCount = 19)
  //  statusUi = statusUi.copy(favCount = 20)
  //  println(statusUi.favCount) // 20
  //  println(statusUi.actionable.favCount) // 20 !!
  actionable = actionableStatus,
  hasUnloadedStatus = hasUnloadedStatus
)

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
    poll = poll,
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
