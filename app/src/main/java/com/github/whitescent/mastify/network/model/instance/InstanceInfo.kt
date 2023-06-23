package com.github.whitescent.mastify.network.model.instance

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InstanceInfo(
  val title: String,
  val description: String,
  val thumbnail: Thumbnail,
  val usage: UsageData
)

@Serializable
data class Thumbnail(
  val url: String
)

@Serializable
data class UsageData(
  val users: ActiveUsersData
)

@Serializable
data class ActiveUsersData(
  @SerialName("active_month") val activeMonth: Int
)
