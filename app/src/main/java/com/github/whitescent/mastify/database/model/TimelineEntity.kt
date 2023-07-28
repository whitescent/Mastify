package com.github.whitescent.mastify.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.github.whitescent.mastify.network.model.account.Account
import com.github.whitescent.mastify.network.model.status.Status

@Entity(
  foreignKeys = [
    ForeignKey(
      entity = AccountEntity::class,
      parentColumns = ["id"],
      childColumns = ["timelineUserId"]
    )
  ],
  indices = [Index("timelineUserId")]
)
data class TimelineEntity(
  @ColumnInfo val id: String,
  @PrimaryKey val uuid: String,
  @ColumnInfo val timelineUserId: Long,
  @ColumnInfo val createdAt: String,
  @ColumnInfo val sensitive: Boolean,
  @ColumnInfo val spoilerText: String,
  @ColumnInfo val visibility: String,
  @ColumnInfo val uri: String,
  @ColumnInfo val url: String?,
  @ColumnInfo val repliesCount: Int,
  @ColumnInfo val reblogsCount: Int,
  @ColumnInfo val inReplyToId: String?,
  @ColumnInfo val inReplyToAccountId: String?,
  @ColumnInfo val favouritesCount: Int,
  @ColumnInfo val editedAt: String?,
  @ColumnInfo val favourited: Boolean,
  @ColumnInfo val reblogged: Boolean,
  @ColumnInfo val reblog: Status?,
  @ColumnInfo val content: String,
  @ColumnInfo val account: Account,
  @ColumnInfo val tags: List<Status.Tag>,
  @ColumnInfo val mentions: List<Status.Mention>,
  @ColumnInfo val application: Status.Application?,
  @ColumnInfo val attachments: List<Status.Attachment>,
  @ColumnInfo val replyChainType: Status.ReplyChainType,
  @ColumnInfo val hasUnloadedReplyStatus: Boolean,
  @ColumnInfo val hasUnloadedStatus: Boolean,
  @ColumnInfo val hasMultiReplyStatus: Boolean,
  @ColumnInfo val shouldShow: Boolean
)
