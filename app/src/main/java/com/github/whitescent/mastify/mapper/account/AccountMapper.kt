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

package com.github.whitescent.mastify.mapper.account

import com.github.whitescent.mastify.database.model.AccountEntity
import com.github.whitescent.mastify.network.model.account.Account

fun AccountEntity.toAccount(): Account {
  return Account(
    id = accountId,
    username = username,
    displayName = displayName,
    note = note,
    url = "https://$domain/@$username",
    avatar = profilePictureUrl,
    header = header,
    followersCount = followersCount,
    followingCount = followingCount,
    statusesCount = statusesCount,
    createdAt = createdAt,
    source = null,
    emojis = emojis,
    fields = fields
  )
}
