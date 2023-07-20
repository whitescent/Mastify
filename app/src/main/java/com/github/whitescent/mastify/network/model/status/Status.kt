package com.github.whitescent.mastify.network.model.status

import androidx.compose.runtime.Immutable
import com.github.whitescent.mastify.database.model.TimelineEntity
import com.github.whitescent.mastify.network.model.status.Status.ReplyChainType.End
import com.github.whitescent.mastify.network.model.status.Status.ReplyChainType.Null
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
  val replyChainType: ReplyChainType = Null,
  val hasUnloadedReplyStatus: Boolean = false,
  val hasUnloadedStatus: Boolean = false,
  val hasMultiReplyStatus: Boolean = false,
  val shouldShow: Boolean = true,
  val uuid: String = id
) {

  val actionableId inline get() = reblog?.id ?: id
  val actionableStatus: Status inline get() = reblog ?: this

  val isInReplyTo inline get() = inReplyToId != null

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
    val fullName: String get() = "@$username@${FormatFactory.getInstanceName(url)}"
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
      uuid = uuid,
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
      replyChainType = replyChainType,
      hasUnloadedReplyStatus = hasUnloadedReplyStatus,
      hasUnloadedStatus = hasUnloadedStatus,
      hasMultiReplyStatus = hasMultiReplyStatus,
      shouldShow = shouldShow
    )
  }

  enum class ReplyChainType {
    Start, Continue, End, Null
  }

  data class ViewData(
    val status: Status,
    val replyChainType: ReplyChainType = status.replyChainType,
    val hasUnloadedReplyStatus: Boolean = status.hasUnloadedReplyStatus,
    val hasUnloadedStatus: Boolean = status.hasUnloadedStatus,
    val hasMultiReplyStatus: Boolean = status.hasMultiReplyStatus,
    val shouldShow: Boolean = status.shouldShow
  ) {
    val id inline get() = status.id
    val uuid inline get() = status.uuid
    val reblog inline get() = status.reblog

    val avatar inline get() = status.reblog?.account?.avatar ?: status.account.avatar
    val rebloggedAvatar inline get() = status.account.avatar

    val displayName inline get() = status.reblog?.account?.displayName?.ifEmpty {
      status.reblog.account.username
    } ?: status.account.displayName.ifEmpty { status.account.username }

    val reblogDisplayName inline get() = status.account.displayName
      .ifEmpty { status.account.username }

    val fullname inline get() = status.reblog?.account?.fullName ?: status.account.fullName
    val createdAt inline get() = status.reblog?.createdAt ?: status.createdAt
    val content inline get() = status.reblog?.content ?: status.content
    val application inline get() = status.reblog?.application ?: status.application
    val sensitive inline get() = status.reblog?.sensitive ?: status.sensitive
    val spoilerText inline get() = status.reblog?.spoilerText ?: status.spoilerText
    val mentions inline get() = status.reblog?.mentions ?: status.mentions
    val tags inline get() = status.reblog?.tags ?: status.tags
    val attachments inline get() = status.reblog?.attachments ?: status.attachments
    val repliesCount inline get() = status.reblog?.repliesCount ?: status.repliesCount
    val reblogsCount inline get() = status.reblog?.reblogsCount ?: status.reblogsCount
    val favouritesCount inline get() = status.reblog?.favouritesCount ?: status.favouritesCount
    val favourited inline get() = status.reblog?.favourited ?: status.favourited

    val actionable: Status inline get() = status.actionableStatus
    val actionableId: String inline get() = status.actionableStatus.id

    val hasOmittedReplyStatus inline get() = hasUnloadedReplyStatus || hasMultiReplyStatus
    val isReplyEnd inline get() = replyChainType == Null || replyChainType == End
    val isInReplyTo inline get() = status.inReplyToId != null
  }
}
