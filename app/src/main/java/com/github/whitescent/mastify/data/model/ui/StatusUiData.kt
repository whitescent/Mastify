package com.github.whitescent.mastify.data.model.ui

import androidx.compose.runtime.Immutable
import com.github.whitescent.mastify.network.model.status.Status
import com.github.whitescent.mastify.network.model.status.Status.ReplyChainType.End
import com.github.whitescent.mastify.network.model.status.Status.ReplyChainType.Null
import kotlinx.collections.immutable.ImmutableList

@Immutable
data class StatusUiData(
  val id: String,
  val uuid: String,
  val reblog: Status?,
  val accountId: String,
  val avatar: String,
  val application: Status.Application?,
  val rebloggedAvatar: String,
  val displayName: String,
  val content: String,
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
  val replyChainType: Status.ReplyChainType,
  val hasUnloadedReplyStatus: Boolean,
  val hasUnloadedStatus: Boolean,
  val hasMultiReplyStatus: Boolean,
  val shouldShow: Boolean
) {
  val hasOmittedReplyStatus inline get() = hasUnloadedReplyStatus || hasMultiReplyStatus
  val isReplyEnd inline get() = replyChainType == Null || replyChainType == End
  val isInReplyTo inline get() = inReplyToId != null
}
