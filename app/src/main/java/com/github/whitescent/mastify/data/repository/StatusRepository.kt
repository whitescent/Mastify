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

package com.github.whitescent.mastify.data.repository

import at.connyduck.calladapter.networkresult.fold
import com.github.whitescent.mastify.network.MastodonApi
import com.github.whitescent.mastify.network.model.status.Status
import javax.inject.Inject

class StatusRepository @Inject constructor(
  private val api: MastodonApi
) {
  suspend fun getAccountStatus(
    onlyMedia: Boolean? = null,
    excludeReplies: Boolean? = null,
    accountId: String,
    maxId: String?,
  ): Result<List<Status>> =
    api.accountStatuses(
      accountId = accountId,
      maxId = maxId,
      excludeReplies = excludeReplies,
      onlyMedia = onlyMedia
    ).fold(
      { statusList ->
        var temp: MutableList<Status> = mutableListOf()
        excludeReplies?.let {
          if (!excludeReplies) {
            temp = statusList.toMutableList()
            statusList.forEach { status ->
              if (status.isInReplyTo) {
                api.status(status.inReplyToId!!).fold(
                  { repliedStatus ->
                    temp.add(temp.indexOf(status), repliedStatus)
                  },
                  { e ->
                    e.printStackTrace()
                  }
                )
              }
            }
          }
        }
        Result.success(temp.ifEmpty { statusList })
      },
      {
        Result.failure(it)
      }
    )
}
