package com.github.whitescent.mastify.core.network.test.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FakeResponse(
  val id: String,
  @SerialName("replies_count") val repliesCount: Int,
  @SerialName("in_reply_to_id") val inReplyToId: String?
)
