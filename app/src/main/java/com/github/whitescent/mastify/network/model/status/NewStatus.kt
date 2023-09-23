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

package com.github.whitescent.mastify.network.model.status

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NewStatus(
  val status: String,
  @SerialName("spoiler_text") val warningText: String,
  @SerialName("in_reply_to_id") val inReplyToId: String?,
  val visibility: String,
  val sensitive: Boolean,
  @SerialName("media_ids") val mediaIds: List<String>?,
  @SerialName("media_attributes") val mediaAttributes: List<MediaAttribute>?,
  @SerialName("scheduled_at") val scheduledAt: String?,
  val poll: NewPoll?,
  val language: String?
)

@Serializable
data class NewPoll(
  val options: List<String>,
  @SerialName("expires_in") val expiresIn: Int,
  val multiple: Boolean
)

// It would be nice if we could reuse MediaToSend,
// but the server requires a different format for focus
@Serializable
data class MediaAttribute(
  val id: String,
  val description: String?,
  val focus: String?,
  val thumbnail: String?
)
