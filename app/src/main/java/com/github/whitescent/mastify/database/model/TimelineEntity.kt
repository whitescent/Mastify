/*
 * Copyright 2023 WhiteScent
 *
 * This file is a part of Mastify.
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * Mastify is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Mastify; if not,
 * see <http://www.gnu.org/licenses>.
 */

package com.github.whitescent.mastify.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.github.whitescent.mastify.network.model.account.Account
import com.github.whitescent.mastify.network.model.emoji.Emoji
import com.github.whitescent.mastify.network.model.status.Hashtag
import com.github.whitescent.mastify.network.model.status.Status
import com.github.whitescent.mastify.network.model.status.Status.Application
import com.github.whitescent.mastify.network.model.status.Status.Attachment
import com.github.whitescent.mastify.network.model.status.Status.Mention

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
  @ColumnInfo val tags: List<Hashtag>,
  @ColumnInfo val mentions: List<Mention>,
  @ColumnInfo val application: Application?,
  @ColumnInfo val attachments: List<Attachment>,
  @ColumnInfo(defaultValue = "false") val hasUnloadedStatus: Boolean
)
