package com.github.whitescent.mastify.data.model.ui

import androidx.compose.runtime.Immutable
import com.github.whitescent.mastify.network.model.account.Account
import com.github.whitescent.mastify.network.model.emoji.Emoji
import com.github.whitescent.mastify.network.model.status.Status
import kotlinx.collections.immutable.ImmutableList
import org.jsoup.Jsoup

@Immutable
data class StatusUiData(
  val id: String,
  val reblog: Status?,
  val link: String,
  val accountId: String,
  val avatar: String,
  val account: Account,
  val application: Status.Application?,
  val reblogged: Boolean,
  val bookmarked: Boolean,
  val visibility: Visibility,
  val rebloggedAvatar: String,
  val displayName: String,
  val content: String,
  val accountEmojis: ImmutableList<Emoji>,
  val emojis: ImmutableList<Emoji>,
  val attachments: ImmutableList<Status.Attachment>,
  val actionable: Status,
  val actionableId: String,
  val reblogDisplayName: String,
  val fullname: String,
  val createdAt: String,
  val sensitive: Boolean,
  val spoilerText: String,
  val repliesCount: Int,
  val reblogsCount: Int,
  val favouritesCount: Int,
  val favorited: Boolean,
  val inReplyToId: String?,
  val hasUnloadedStatus: Boolean,
) {

  val parsedContent: String = Jsoup.parse(content).body().text()
  val isInReplyTo inline get() = inReplyToId != null

  enum class ReplyChainType {
    Start, Continue, End, Null
  }

  enum class Visibility {

    Public, Unlisted, Private, Direct, Unknown;

    val rebloggingAllowed get() = (this == Public || this == Unlisted)

    override fun toString(): String {
      return when (this) {
        Public -> "public"
        Unlisted -> "unlisted"
        Private -> "private"
        Direct -> "direct"
        Unknown -> "unknown"
      }
    }

    companion object {
      fun byString(s: String): Visibility {
        return when (s) {
          "public" -> Public
          "unlisted" -> Unlisted
          "private" -> Private
          "direct" -> Direct
          "unknown" -> Unknown
          else -> Unknown
        }
      }
    }
  }

  companion object {
    const val statusContentType = "status"
  }
}
