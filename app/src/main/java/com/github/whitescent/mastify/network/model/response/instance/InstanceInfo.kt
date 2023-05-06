package com.github.whitescent.mastify.network.model.response.instance

import kotlinx.serialization.Serializable

@Serializable
data class InstanceInfo(
  val title: String,
  val description: String,
  val thumbnail: Thumbnail
)

@Serializable
data class Thumbnail(
  val url: String
)
