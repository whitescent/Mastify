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

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.github.whitescent.R
import com.github.whitescent.mastify.network.MastodonApi
import com.github.whitescent.mastify.utils.getOrThrow
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class NotificationRepository @Inject constructor(
  private val api: MastodonApi,
  @ApplicationContext val context: Context
) {
  suspend fun getAccountNotifications(
    maxId: String? = null,
    minId: String? = null,
    types: List<String>? = null,
    limit: Int? = null,
    excludes: List<String>? = null
  ) = api.notifications(maxId, minId, types, limit, excludes).getOrThrow()

  companion object {
    fun createWorkerNotificationChannel(context: Context) {
      val notificationManager = context.getSystemService(NotificationManager::class.java)
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = context.getString(R.string.notification_background_work_channel_name)
        val descriptionText =
          context.getString(
            R.string.notification_background_work_channel_description
          )
        val importance = NotificationManager.IMPORTANCE_NONE
        val channel = NotificationChannel(WORK_CHANNEL_NAME, name, importance)
        channel.description = descriptionText
        channel.setShowBadge(true)
        notificationManager.createNotificationChannel(channel)
      }
    }

    private const val WORK_CHANNEL_NAME = "CHANNEL_BACKGROUND_TASK"
  }
}