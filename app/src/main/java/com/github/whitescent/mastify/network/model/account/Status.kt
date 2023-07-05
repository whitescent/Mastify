package com.github.whitescent.mastify.network.model.account

import androidx.compose.runtime.Immutable
import com.github.whitescent.mastify.database.model.TimelineEntity
import com.github.whitescent.mastify.utils.FormatFactory
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Immutable
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
  @SerialName("in_reply_to_id") val inReplyToId: String?,
  @SerialName("in_reply_to_account_id") val inReplyToAccountId: String?,
  val reblogged: Boolean,
  val reblog: Status?,
  val content: String,
  val account: Account,
  val tags: List<Tag>,
  val mentions: List<Mention>,
  val application: Application?,
  @SerialName("media_attachments") val attachments: List<Attachment>,
  val hasReplyStatus: Boolean = false,
  val betweenInReplyStatus: Boolean = false,
  val isLastReplyStatus: Boolean = false,
  val hasUnloadedReplyStatus: Boolean = false
) {

  val isInReplyTo get() = inReplyToId != null

  @Serializable
  data class Tag(val name: String, val url: String, val following: Boolean? = null)

  @Serializable
  data class Application(val name: String, val website: String?)

  @Serializable
  data class Attachment(
    val url: String,
    val type: String,
    @SerialName("preview_url") val previewUrl: String?
  )

  @Serializable
  data class Account(
    val id: String,
    @SerialName("display_name") val displayName: String,
    val username: String,
    val avatar: String,
    val url: String
  ) {
    val fullName: String
      get() = "@$username@${FormatFactory.getInstanceName(url)}"
  }

  @Serializable
  data class Mention(
    val id: String,
    val username: String,
    val url: String,
    val acct: String
  )

  fun toEntity(timelineUserId: Long): TimelineEntity {
    return TimelineEntity(
      id = id,
      timelineUserId = timelineUserId,
      createdAt = createdAt,
      sensitive = sensitive,
      spoilerText = spoilerText,
      visibility = visibility,
      uri = uri,
      url = url,
      repliesCount = repliesCount,
      reblogsCount = reblogsCount,
      favouritesCount = favouritesCount,
      editedAt = editedAt,
      favourited = favourited,
      inReplyToId = inReplyToId,
      inReplyToAccountId = inReplyToAccountId,
      reblogged = reblogged,
      reblog = reblog,
      content = content,
      tags = tags,
      mentions = mentions,
      account = account,
      application = application,
      attachments = attachments,
      hasReplyStatus = hasReplyStatus,
      betweenInReplyStatus = betweenInReplyStatus,
      isLastReplyStatus = isLastReplyStatus,
      hasUnloadedReplyStatus = hasUnloadedReplyStatus,
    )
  }
}
