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

package com.github.whitescent.mastify.network.model.instance

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InstanceInfo(
  val title: String,
  @SerialName("short_description") val shortDescription: String,
  val thumbnail: String,
  val stats: UsageData,
  val configuration: Configuration?
) {

  @Serializable
  data class Configuration(
    val accounts: AccountsConfiguration?,
    val statuses: StatusesConfiguration?,
    val polls: PollConfiguration?,
    @SerialName("media_attachments") val mediaAttachments: MediaAttachmentsConfiguration
  ) {
    @Serializable
    data class AccountsConfiguration(
      @SerialName("max_featured_tags") val maxFeaturedTags: Int?
    )

    @Serializable
    data class StatusesConfiguration(
      @SerialName("max_characters") val maxCharacters: Int?,
      @SerialName("max_media_attachments") val maxMediaAttachments: Int?
    )

    @Serializable
    data class PollConfiguration(
      @SerialName("max_options") val maxOptions: Int?,
      @SerialName("max_option_chars") val maxOptionChars: Int?,
      @SerialName("max_characters_per_option") val maxCharactersPerOption: Int?,
      @SerialName("min_expiration") val minExpiration: Int?,
      @SerialName("max_expiration") val maxExpiration: Int?
    )

    @Serializable
    data class MediaAttachmentsConfiguration(
      @SerialName("supported_mime_types") val supportedMimeTypes: List<String>?,
      @SerialName("image_size_limit") val imageSizeLimit: Int?,
      @SerialName("image_matrix_limit") val imageMatrixLimit: Int?,
      @SerialName("video_size_limit") val videoSizeLimit: Int?,
      @SerialName("video_frame_rate_limit") val videoFrameRateLimit: Int?,
      @SerialName("video_matrix_limit") val videoMatrixLimit: Int?
    )
  }

  @Serializable
  data class UsageData(
    @SerialName("user_count") val userCount: Int
  )
}
