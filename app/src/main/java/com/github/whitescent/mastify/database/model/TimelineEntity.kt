package com.github.whitescent.mastify.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.github.whitescent.mastify.network.model.account.Status

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
  @ColumnInfo val favouritesCount: Int,
  @ColumnInfo val editedAt: String?,
  @ColumnInfo val favourited: Boolean,
  @ColumnInfo val reblogged: Boolean,
  @ColumnInfo val reblog: Status?,
  @ColumnInfo val content: String,
  @ColumnInfo val account: Status.Account,
  @ColumnInfo val tags: List<Status.Tag>,
  @ColumnInfo val mentions: List<Status.Mention>,
  @ColumnInfo val application: Status.Application?,
  @ColumnInfo val attachments: List<Status.Attachment>
)
