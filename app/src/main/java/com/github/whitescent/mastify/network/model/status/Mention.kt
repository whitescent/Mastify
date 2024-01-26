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

package com.github.whitescent.mastify.network.model.status

import com.github.whitescent.mastify.network.model.account.Account
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class Mention(
  val id: String,
  val username: String,
  val url: String,
  val acct: String
) {
  fun toAccount() = Account(
    id = id,
    username = username,
    displayName = username,
    note = "",
    url = url,
    avatar = url,
    header = "",
    followersCount = 0,
    followingCount = 0,
    statusesCount = 0,
    createdAt = Instant.now().toString(),
    source = null,
    fields = emptyList(),
    emojis = emptyList()
  )
}
