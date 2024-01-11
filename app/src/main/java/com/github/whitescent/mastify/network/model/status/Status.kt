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

package com.github.whitescent.mastify.network.model.status

import com.github.whitescent.mastify.network.model.account.Account
import com.github.whitescent.mastify.network.model.emoji.Emoji
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Status(
  val id: String,
  @SerialName("created_at") val createdAt: String,
  val sensitive: Boolean,
  @SerialName("spoiler_text") val spoilerText: String,
  val visibility: String,
  val uri: String,
  val url: String?,
  @SerialName("replies_count") val repliesCount: Int,
  @SerialName("reblogs_count") val reblogsCount: Int,
  @SerialName("favourites_count") val favouritesCount: Int,
  @SerialName("edited_at") val editedAt: String?,
  @SerialName("favourited") val favorited: Boolean, // I prefer American English lol
  @SerialName("in_reply_to_id") val inReplyToId: String?,
  @SerialName("in_reply_to_account_id") val inReplyToAccountId: String?,
  val reblogged: Boolean,
  val bookmarked: Boolean,
  val reblog: Status?,
  val content: String,
  val account: Account,
  val poll: Poll?,
  val emojis: List<Emoji>,
  val tags: List<Hashtag>,
  val mentions: List<Mention>,
  val application: Application?,
  @SerialName("media_attachments") val attachments: List<Attachment>,
  val hasUnloadedStatus: Boolean = false,
) {

  val actionableId inline get() = reblog?.id ?: id
  val actionableStatus inline get() = reblog ?: this

  val isInReplyTo inline get() = inReplyToId != null

  @Serializable
  data class Application(val name: String, val website: String?)

  @Serializable
  data class Attachment(
    val url: String,
    val type: String,
    @SerialName("preview_url") val previewUrl: String?,
    val blurhash: String?,
    val meta: Meta?
  )

  @Serializable
  data class Meta(
    val original: Original,
  ) {
    @Serializable
    data class Original(
      val duration: Float?,
      val width: Int = 0,
      val height: Int = 0,
    )
  }

  @Serializable
  data class Mention(
    val id: String,
    val username: String,
    val url: String,
    val acct: String
  )
}
