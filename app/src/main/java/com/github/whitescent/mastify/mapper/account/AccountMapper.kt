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
    fields = fields
  )
}
