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
