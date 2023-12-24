/*
 * Copyright 2023 WhiteScent
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

package com.github.whitescent.mastify.network.model.account

import androidx.compose.runtime.Stable
import com.github.whitescent.mastify.network.model.emoji.Emoji
import com.github.whitescent.mastify.ui.component.generateHtmlContentWithEmoji
import com.github.whitescent.mastify.utils.FormatFactory
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Stable
@Serializable
data class Account(
  val id: String,
  val username: String,
  @SerialName("display_name") val displayName: String,
  val note: String,
  val url: String,
  val avatar: String,
  val header: String,
  @SerialName("followers_count") val followersCount: Long,
  @SerialName("following_count") val followingCount: Long,
  @SerialName("statuses_count") val statusesCount: Long,
  @SerialName("created_at") val createdAt: String,
  val source: Source?,
  val fields: List<Fields>,
  val emojis: List<Emoji>
) {

  val noteWithEmoji = generateHtmlContentWithEmoji(note, emojis)
  val fieldsWithEmoji = fields.map {
    it.copy(
      name = generateHtmlContentWithEmoji(it.name, emojis),
      value = generateHtmlContentWithEmoji(it.value, emojis)
    )
  }
  val isEmptyHeader = header.contains("missing.png")

  val domain get() = FormatFactory.getInstanceName(url)

  // avoid null display name
  val realDisplayName inline get() = this.displayName.ifEmpty { this.username }

  val fullname: String
    get() = "@$username@$domain"
}

@Serializable
data class Source(
  val note: String,
  val fields: List<Fields>
)

@Serializable
data class Fields(
  val name: String,
  val value: String,
  @SerialName("verified_at") val verifiedAt: String?
)
