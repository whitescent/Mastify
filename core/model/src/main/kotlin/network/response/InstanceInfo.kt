package com.github.whitescent.mastify.core.model.network.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InstanceInfo(
  @SerialName("title") val title: String,
  @SerialName("short_description") val shortDescription: String,
  @SerialName("thumbnail") val thumbnail: String,
  @SerialName("stats") val stats: UsageData,
  @SerialName("configuration") val configuration: Configuration?
) {

  @Serializable
  data class Configuration(
    @SerialName("accounts") val accounts: AccountsConfiguration?,
    @SerialName("statuses") val statuses: StatusesConfiguration?,
    @SerialName("polls") val polls: PollConfiguration?,
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
