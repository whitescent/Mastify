package com.github.whitescent.mastify.core.model.network.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Account(
  @SerialName("id")
  val id: String,
  @SerialName("username")
  val username: String,
  @SerialName("display_name")
  val displayName: String,
  @SerialName("note")
  val note: String,
  @SerialName("url")
  val url: String,
  @SerialName("avatar")
  val avatar: String,
  @SerialName("header")
  val header: String,
  @SerialName("followers_count")
  val followersCount: Long,
  @SerialName("following_count")
  val followingCount: Long,
  @SerialName("statuses_count")
  val statusesCount: Long,
  @SerialName("created_at")
  val createdAt: String,
  @SerialName("source")
  val source: Source?,
  @SerialName("fields")
  val fields: List<Fields>
)

@Serializable
data class Source(
  @SerialName("note")
  val note: String,
  @SerialName("fields")
  val fields: List<Fields>
)

@Serializable
data class Fields(
  @SerialName("name")
  val name: String,
  @SerialName("value")
  val value: String,
  @SerialName("verified_at")
  val verifiedAt: String?
)
