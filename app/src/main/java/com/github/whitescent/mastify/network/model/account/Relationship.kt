package com.github.whitescent.mastify.network.model.account

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Relationship(
  val id: String,
  val following: Boolean,
  @SerialName("followed_by") val followedBy: Boolean,
  val blocking: Boolean,
  val muting: Boolean,
  @SerialName("muting_notifications") val mutingNotifications: Boolean,
  val requested: Boolean,
  @SerialName("showing_reblogs") val showingReblogs: Boolean,
  @SerialName("domain_blocking") val blockingDomain: Boolean,
  val note: String?,
  val notifying: Boolean?
)
