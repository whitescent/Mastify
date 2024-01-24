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

import at.connyduck.calladapter.networkresult.getOrElse
import com.github.whitescent.mastify.database.AppDatabase
import com.github.whitescent.mastify.database.model.EmojisEntity
import com.github.whitescent.mastify.database.model.InstanceEntity
import com.github.whitescent.mastify.database.model.InstanceInfoEntity
import com.github.whitescent.mastify.network.MastodonApi
import com.github.whitescent.mastify.network.model.emoji.Emoji
import javax.inject.Inject

class InstanceRepository @Inject constructor(
  db: AppDatabase,
  private val api: MastodonApi,
) {

  private val instanceDao = db.instanceDao()
  private val accountDao = db.accountDao()

  suspend fun getInstanceEmojis(): List<Emoji> {
    val instanceName = accountDao.getActiveAccount()!!.domain
    return instanceDao.getEmojiInfo(instanceName)?.emojiList ?: emptyList()
  }

  suspend fun getInstanceInfo(): InstanceEntity? {
    val instanceName = accountDao.getActiveAccount()!!.domain
    return instanceDao.getInstanceInfo(instanceName)
  }

  suspend fun upsertEmojis(): Boolean {
    api.getCustomEmojis().getOrElse {
      return false
    }.let {
      instanceDao.upsert(EmojisEntity(accountDao.getActiveAccount()!!.domain, it))
      return true
    }
  }

  suspend fun upsertInstanceInfo(): Boolean {
    api.fetchInstanceInfo().getOrElse {
      return false
    }.let { instance ->
      val instanceName = accountDao.getActiveAccount()!!.domain
      instanceDao.upsert(
        instance = InstanceInfoEntity(
          instance = instanceName,
          maximumTootCharacters = instance.configuration?.statuses?.maxCharacters,
          maxPollOptions = instance.configuration?.polls?.maxOptions,
          maxPollCharactersPerOption = instance.configuration?.polls?.maxCharactersPerOption,
          minPollExpiration = instance.configuration?.polls?.minExpiration,
          maxPollExpiration = instance.configuration?.polls?.maxExpiration,
          videoSizeLimit = instance.configuration?.mediaAttachments?.videoSizeLimit,
          imageSizeLimit = instance.configuration?.mediaAttachments?.imageSizeLimit,
          imageMatrixLimit = instance.configuration?.mediaAttachments?.imageMatrixLimit,
          maxMediaAttachments = instance.configuration?.statuses?.maxMediaAttachments,
        )
      )
      return true
    }
  }

  companion object {
    private const val TAG = "InstanceInfoRepo"

    const val DEFAULT_CHARACTER_LIMIT = 500
    const val DEFAULT_MAX_OPTION_COUNT = 4
    const val DEFAULT_MAX_OPTION_LENGTH = 50
    private const val DEFAULT_MAX_POLL_DURATION = 604800
    private const val DEFAULT_MIN_POLL_DURATION = 300

    private const val DEFAULT_VIDEO_SIZE_LIMIT = 41943040 // 40MiB
    private const val DEFAULT_IMAGE_SIZE_LIMIT = 10485760 // 10MiB
    private const val DEFAULT_IMAGE_MATRIX_LIMIT = 16777216 // 4096^2 Pixels

    const val DEFAULT_MAX_MEDIA_ATTACHMENTS = 4
  }
}
