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

package com.github.whitescent.mastify.data.model.ui

import androidx.compose.runtime.Immutable
import com.github.whitescent.mastify.network.model.account.Account
import com.github.whitescent.mastify.network.model.emoji.Emoji
import com.github.whitescent.mastify.network.model.status.Card
import com.github.whitescent.mastify.network.model.status.Hashtag
import com.github.whitescent.mastify.network.model.status.Mention
import com.github.whitescent.mastify.network.model.status.Poll
import com.github.whitescent.mastify.network.model.status.Status
import com.github.whitescent.mastify.ui.component.buildPlainText
import kotlinx.collections.immutable.ImmutableList

@Immutable
data class StatusUiData(
  val id: String,
  val reblog: Status?,
  val link: String,
  val accountId: String,
  val avatar: String,
  val account: Account,
  val application: Status.Application?,
  val reblogged: Boolean,
  val bookmarked: Boolean,
  val visibility: Visibility,
  val rebloggedAvatar: String,
  val displayName: String,
  val content: String,
  val accountEmojis: ImmutableList<Emoji>,
  val emojis: ImmutableList<Emoji>,
  val tags: ImmutableList<Hashtag>,
  val mentions: ImmutableList<Mention>,
  val attachments: ImmutableList<Status.Attachment>,
  val actionable: Status,
  val reblogDisplayName: String,
  val fullname: String,
  val createdAt: String,
  val sensitive: Boolean,
  val poll: Poll?,
  val card: Card?,
  val spoilerText: String,
  val repliesCount: Int,
  val reblogsCount: Int,
  val favouritesCount: Int,
  val favorited: Boolean,
  val inReplyToId: String?,
  val inReplyToAccountId: String?,
  val hasUnloadedStatus: Boolean,
) {

  val actionableId inline get() = reblog?.id ?: id

  val parsedContent: String = buildPlainText(content, false)
  val isInReplyTo inline get() = inReplyToId != null
  val hasVisibleText: Boolean = buildPlainText(content, isInReplyToSomeone).trim().isNotEmpty()

  val isInReplyToSomeone inline get() = mentions.size == 1 && isInReplyTo &&
    inReplyToAccountId != accountId

  enum class ReplyChainType {
    Start, Continue, End, Null
  }

  sealed class Visibility {
    object Public : Visibility()
    object Private : Visibility()
    object Unlisted : Visibility()
    object Direct : Visibility()

    val rebloggingAllowed get() = this == Public || this == Unlisted

    override fun toString(): String {
      return when (this) {
        Public -> "public"
        Unlisted -> "unlisted"
        Private -> "private"
        Direct -> "direct"
      }
    }

    companion object {
      val entries = listOf(Public, Private, Unlisted, Direct)

      fun byString(s: String): Visibility {
        return when (s) {
          "public" -> Public
          "unlisted" -> Unlisted
          "private" -> Private
          "direct" -> Direct
          else -> Public
        }
      }
    }
  }

  companion object
}
