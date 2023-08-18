package com.github.whitescent.mastify.network.model.emoji

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Emoji(
  val shortcode: String,
  val url: String,
  @SerialName("static_url") val staticUrl: String,
  @SerialName("visible_in_picker") val visibleInPicker: Boolean?
)
