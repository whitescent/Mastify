package com.github.whitescent.mastify.network.model.response.account

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Status(
  val id: String,
  @SerialName("created_at") val createdAt: String,
  val sensitive: Boolean,
  @SerialName("spoiler_text") val spoilerText: String,
  val visibility: String,
  val uri: String,
  val url: String?,
  @SerialName("replies_count") val repliesCount: Int,
  @SerialName("reblogs_count") val reblogsCount: Int,
  @SerialName("favourites_count") val favouritesCount: Int,
  @SerialName("edited_at") val editedAt: String?,
  val favourited: Boolean,
  val reblogged: Boolean,
  val reblog: Reblog?,
  val content: String,
  val account: Account,
  @SerialName("media_attachments") val mediaAttachments: List<MediaAttachments>
)

@Serializable
data class Reblog(
  val id: String,
  @SerialName("created_at") val createdAt: String,
  @SerialName("replies_count") val repliesCount: Int,
  @SerialName("reblogs_count") val reblogsCount: Int,
  @SerialName("favourites_count") val favouritesCount: Int,
  val content: String,
  val account: Account,
  @SerialName("media_attachments") val mediaAttachments: List<MediaAttachments>
)

@Serializable
data class MediaAttachments(
  val url: String,
  val type: String,
  @SerialName("preview_url") val previewUrl: String
)

@Serializable
data class Account(
  val id: String,
  @SerialName("display_name") val displayName: String,
  val username: String,
  val avatar: String,
  val url: String
)

@Serializable
data class Application(
  val name: String,
  val website: String
)
