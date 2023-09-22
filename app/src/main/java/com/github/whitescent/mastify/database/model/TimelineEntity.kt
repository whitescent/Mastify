package com.github.whitescent.mastify.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.github.whitescent.mastify.network.model.account.Account
import com.github.whitescent.mastify.network.model.emoji.Emoji
import com.github.whitescent.mastify.network.model.status.Status
import com.github.whitescent.mastify.network.model.status.Status.Application
import com.github.whitescent.mastify.network.model.status.Status.Attachment
import com.github.whitescent.mastify.network.model.status.Status.Mention
import com.github.whitescent.mastify.network.model.status.Status.Tag

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
  @PrimaryKey val id: String,
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
  @ColumnInfo val favorited: Boolean,
  @ColumnInfo val reblogged: Boolean,
  @ColumnInfo val bookmarked: Boolean,
  @ColumnInfo val reblog: Status?,
  @ColumnInfo val content: String,
  @ColumnInfo val account: Account,
  @ColumnInfo val emojis: List<Emoji>,
  @ColumnInfo val tags: List<Tag>,
  @ColumnInfo val mentions: List<Mention>,
  @ColumnInfo val application: Application?,
  @ColumnInfo val attachments: List<Attachment>,
  @ColumnInfo(defaultValue = "false") val hasUnloadedStatus: Boolean
)
