package com.github.whitescent.mastify.data.model.ui

import androidx.compose.runtime.Immutable
import com.github.whitescent.mastify.data.model.ui.StatusUiData.ReplyChainType
import com.github.whitescent.mastify.data.model.ui.StatusUiData.ReplyChainType.Continue
import com.github.whitescent.mastify.data.model.ui.StatusUiData.ReplyChainType.End
import com.github.whitescent.mastify.data.model.ui.StatusUiData.ReplyChainType.Null
import com.github.whitescent.mastify.data.model.ui.StatusUiData.ReplyChainType.Start
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
  val favourited: Boolean,
  val inReplyToId: String?,
) {
  val itemType = "status"
  val parsedContent: String = Jsoup.parse(content).body().text()
  val isInReplyTo inline get() = inReplyToId != null

  enum class ReplyChainType {
    Start, Continue, End, Null
  }
}

fun List<StatusUiData>.hasUnloadedParent(index: Int): Boolean {
  val current = get(index)
  val currentType = getReplyChainType(index)
  if (currentType == Null || !current.isInReplyTo) return false
  return when (val prev = getOrNull(index - 1)) {
    null -> false
    else -> current.inReplyToId != prev.id
  }
}

fun List<StatusUiData>.getReplyChainType(index: Int): ReplyChainType {
  val prev = getOrNull(index - 1)
  val current = get(index)
  val next = this.getOrNull(index + 1)

  return when {
    prev != null && next != null -> {
      when {
        (current.isInReplyTo &&
          current.inReplyToId == prev.id && next.inReplyToId == current.id) -> Continue
        next.inReplyToId == current.id -> Start
        current.inReplyToId == prev.id -> End
        else -> Null
      }
    }
    prev == null && next != null -> {
      when (next.inReplyToId) {
        current.id -> Start
        else -> Null
      }
    }
    prev != null && next == null -> {
      when {
        current.isInReplyTo && current.inReplyToId == prev.id -> End
        else -> Null
      }
    }
    else -> Null
  }
}
