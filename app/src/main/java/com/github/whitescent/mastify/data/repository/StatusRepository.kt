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

package com.github.whitescent.mastify.data.repository

import android.content.Context
import android.widget.Toast
import at.connyduck.calladapter.networkresult.fold
import at.connyduck.calladapter.networkresult.getOrThrow
import com.github.whitescent.mastify.data.model.ui.StatusUiData.Visibility
import com.github.whitescent.mastify.network.MastodonApi
import com.github.whitescent.mastify.network.model.status.MediaAttribute
import com.github.whitescent.mastify.network.model.status.NewPoll
import com.github.whitescent.mastify.network.model.status.NewStatus
import com.github.whitescent.mastify.network.model.status.Status
import com.github.whitescent.mastify.network.model.status.StatusContext
import com.github.whitescent.mastify.utils.getServerErrorMessage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.util.UUID
import javax.inject.Inject

class StatusRepository @Inject constructor(
  @ApplicationContext private val context: Context,
  private val api: MastodonApi
) {

  suspend fun getAccountStatus(
    onlyMedia: Boolean? = null,
    excludeReplies: Boolean? = null,
    limit: Int? = null,
    accountId: String,
    maxId: String?,
  ): Result<List<Status>> {
    return api.accountStatuses(
      accountId = accountId,
      maxId = maxId,
      excludeReplies = excludeReplies,
      onlyMedia = onlyMedia,
      limit = limit
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

  suspend fun createStatus(
    content: String,
    warningText: String = "",
    inReplyToId: String? = null,
    visibility: Visibility = Visibility.Public,
    sensitive: Boolean = false,
    mediaIds: List<String>? = null,
    mediaAttributes: List<MediaAttribute>? = null,
    scheduledAt: String? = null,
    poll: NewPoll? = null,
    language: String? = null
  ): Flow<Status> = flow {
    try {
      val response = api.createStatus(
        idempotencyKey = UUID.randomUUID().toString(),
        status = NewStatus(
          status = content,
          warningText = warningText,
          inReplyToId = inReplyToId,
          visibility = visibility.toString(),
          sensitive = sensitive,
          mediaIds = mediaIds,
          mediaAttributes = mediaAttributes,
          scheduledAt = scheduledAt,
          poll = poll,
          language = language,
        )
      )
      if (response.isSuccessful && response.body() != null) emit(response.body()!!)
      else {
        val error = HttpException(response)
        val errorMessage = error.getServerErrorMessage()
        if (errorMessage == null) {
          Toast.makeText(context, error.localizedMessage, Toast.LENGTH_SHORT).show()
          throw error
        } else {
          Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
          throw Throwable(errorMessage)
        }
      }
    } catch (e: Exception) {
      e.printStackTrace()
      Toast.makeText(context, e.localizedMessage, Toast.LENGTH_SHORT).show()
      throw e
    }
  }

  suspend fun getSingleStatus(id: String): Flow<Status> = flow {
    emit(api.status(id).getOrThrow())
  }

  suspend fun getStatusContext(id: String): Flow<StatusContext> = flow {
    emit(api.statusContext(id).getOrThrow())
  }
}
