package com.github.whitescent.mastify.network.model.status

import com.github.whitescent.mastify.network.model.account.Account
import com.github.whitescent.mastify.network.model.emoji.Emoji
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
  @SerialName("in_reply_to_id") val inReplyToId: String?,
  @SerialName("in_reply_to_account_id") val inReplyToAccountId: String?,
  val reblogged: Boolean,
  val bookmarked: Boolean,
  val reblog: Status?,
  val content: String,
  val account: Account,
  val emojis: List<Emoji>,
  val tags: List<Tag>,
  val mentions: List<Mention>,
  val application: Application?,
  @SerialName("media_attachments") val attachments: List<Attachment>
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
    @SerialName("preview_url") val previewUrl: String?,
    val blurhash: String?,
    val meta: Meta?
  )

  @Serializable
  data class Meta(
    val original: Original,
  ) {
    @Serializable
    data class Original(
      val duration: Float?,
      val width: Int = 0,
      val height: Int = 0,
    )
  }

  @Serializable
  data class Mention(
    val id: String,
    val username: String,
    val url: String,
    val acct: String
  )
}
