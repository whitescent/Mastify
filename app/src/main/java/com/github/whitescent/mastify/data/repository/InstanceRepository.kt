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

import android.util.Log
import at.connyduck.calladapter.networkresult.fold
import at.connyduck.calladapter.networkresult.getOrElse
import at.connyduck.calladapter.networkresult.onSuccess
import com.github.whitescent.mastify.data.model.ui.InstanceUiData
import com.github.whitescent.mastify.database.AppDatabase
import com.github.whitescent.mastify.database.model.EmojisEntity
import com.github.whitescent.mastify.database.model.InstanceInfoEntity
import com.github.whitescent.mastify.network.MastodonApi
import com.github.whitescent.mastify.network.model.emoji.Emoji
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class InstanceRepository @Inject constructor(
  db: AppDatabase,
  private val api: MastodonApi,
) {

  private val instanceDao = db.instanceDao()
  private val accountDao = db.accountDao()

  // private val instanceName get() = accountRepository.activeAccount.value!!.domain

  /**
   * Returns the custom emojis of the instance.
   * Will always try to fetch them from the api, falls back to cached Emojis in case it is not available.
   * Never throws, returns empty list in case of error.
   */
  suspend fun getEmojis(): List<Emoji> = withContext(Dispatchers.IO) {
    val instanceName = accountDao.getActiveAccount()!!.domain
    api.getCustomEmojis()
      .onSuccess { emojiList -> instanceDao.upsert(EmojisEntity(instanceName, emojiList)) }
      .getOrElse { throwable ->
        Log.w(TAG, "failed to load custom emojis, falling back to cache", throwable)
        instanceDao.getEmojiInfo(instanceName)?.emojiList.orEmpty()
      }
  }

  /**
   * Returns information about the instance.
   * Will always try to fetch the most up-to-date data from the api, falls back to cache in case it is not available.
   * Never throws, returns defaults of vanilla Mastodon in case of error.
   */
  suspend fun getAndUpdateInstanceInfo(): InstanceUiData = withContext(Dispatchers.IO) {
    val instanceName = accountDao.getActiveAccount()!!.domain
    api.fetchInstanceInfo()
      .fold(
        { instance ->
          val instanceEntity = InstanceInfoEntity(
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
          instanceDao.upsert(instanceEntity)
          instanceEntity
        },
        { throwable ->
          Log.w(TAG, "failed to instance, falling back to cache and default values", throwable)
          instanceDao.getInstanceInfo(instanceName)
        },
      ).let { instanceInfo: InstanceInfoEntity? ->
        InstanceUiData(
          maximumTootCharacters = instanceInfo?.maximumTootCharacters ?: DEFAULT_CHARACTER_LIMIT,
          maxPollOptions = instanceInfo?.maxPollOptions ?: DEFAULT_MAX_OPTION_COUNT,
          maxPollCharactersPerOption = instanceInfo?.maxPollCharactersPerOption ?: DEFAULT_MAX_OPTION_LENGTH,
          minPollExpiration = instanceInfo?.minPollExpiration ?: DEFAULT_MIN_POLL_DURATION,
          maxPollExpiration = instanceInfo?.maxPollExpiration ?: DEFAULT_MAX_POLL_DURATION,
          videoSizeLimit = instanceInfo?.videoSizeLimit ?: DEFAULT_VIDEO_SIZE_LIMIT,
          imageSizeLimit = instanceInfo?.imageSizeLimit ?: DEFAULT_IMAGE_SIZE_LIMIT,
          imageMatrixLimit = instanceInfo?.imageMatrixLimit ?: DEFAULT_IMAGE_MATRIX_LIMIT,
          maxMediaAttachments = instanceInfo?.maxMediaAttachments ?: DEFAULT_MAX_MEDIA_ATTACHMENTS
        )
      }
  }

  companion object {
    private const val TAG = "InstanceInfoRepo"

    const val DEFAULT_CHARACTER_LIMIT = 500
    private const val DEFAULT_MAX_OPTION_COUNT = 4
    private const val DEFAULT_MAX_OPTION_LENGTH = 50
    private const val DEFAULT_MIN_POLL_DURATION = 300
    private const val DEFAULT_MAX_POLL_DURATION = 604800

    private const val DEFAULT_VIDEO_SIZE_LIMIT = 41943040 // 40MiB
    private const val DEFAULT_IMAGE_SIZE_LIMIT = 10485760 // 10MiB
    private const val DEFAULT_IMAGE_MATRIX_LIMIT = 16777216 // 4096^2 Pixels

    const val DEFAULT_MAX_MEDIA_ATTACHMENTS = 4
  }
}
