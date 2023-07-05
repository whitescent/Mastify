package com.github.whitescent.mastify.network.model.instance

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InstanceInfo(
  val title: String,
  @SerialName("short_description") val shortDescription: String,
  val thumbnail: String,
  val stats: UsageData
)

@Serializable
data class UsageData(
  @SerialName("user_count") val userCount: Int
)
