/*
 * Copyright 2024 WhiteScent
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
import androidx.room.PrimaryKey
import com.github.whitescent.mastify.network.model.account.Fields
import com.github.whitescent.mastify.network.model.emoji.Emoji

@Entity
data class AccountEntity(
  @PrimaryKey(autoGenerate = true) val id: Long,
  val domain: String,
  val accessToken: String,
  val clientId: String?, // nullable for backward compatibility
  val clientSecret: String?, // nullable for backward compatibility
  val isActive: Boolean,
  val accountId: String,
  val username: String = "",
  val displayName: String = "",
  val note: String = "",
  val profilePictureUrl: String = "",
  val header: String = "",
  val createdAt: String = "",
  val emojis: List<Emoji>,
  val fields: List<Fields>,
  val followersCount: Long,
  val followingCount: Long,
  val statusesCount: Long,
  @ColumnInfo(defaultValue = "0") val firstVisibleItemIndex: Int,
  @ColumnInfo(defaultValue = "0") val offset: Int,
  val lastNotificationId: String?
) {

  val fullname: String get() = "@$username@$domain"

  val realDisplayName inline get() = this.displayName.ifEmpty { this.username }
  val isEmptyHeader get() = this.header.contains("missing.png")
}
