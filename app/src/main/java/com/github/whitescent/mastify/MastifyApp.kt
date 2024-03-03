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

package com.github.whitescent.mastify

import android.app.Application
import android.os.Build.VERSION.SDK_INT
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.VideoFrameDecoder
import com.github.whitescent.R
import com.github.whitescent.mastify.data.repository.NotificationRepository.Companion.createWorkerNotificationChannel
import com.github.whitescent.mastify.database.AppDatabase
import com.github.whitescent.mastify.work.TimelineWork
import com.github.whitescent.mastify.work.TimelineWorkFactory
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import logcat.AndroidLogcatLogger
import logcat.LogPriority

@HiltAndroidApp
class MastifyApp : Application(), ImageLoaderFactory {
  @Inject
  lateinit var db: AppDatabase

  override fun newImageLoader(): ImageLoader {
    val context = this.applicationContext
    return ImageLoader.Builder(context)
      .components {
        if (SDK_INT >= 28) {
          add(ImageDecoderDecoder.Factory())
        } else {
          add(GifDecoder.Factory())
        }
        add(VideoFrameDecoder.Factory())
      }
      .placeholder(R.drawable.image_placeholder)
      .crossfade(true)
      .build()
  }

  override fun onCreate() {
    super.onCreate()
    createWorkerNotificationChannel(this.applicationContext)

    WorkManager.initialize(
      this,
      Configuration.Builder()
        .setMinimumLoggingLevel(android.util.Log.DEBUG)
        .setWorkerFactory(TimelineWorkFactory(db))
        .build()
    )

    val pruneCacheWorker = PeriodicWorkRequestBuilder<TimelineWork>(6, TimeUnit.HOURS)
      .setConstraints(Constraints.Builder().setRequiresDeviceIdle(true).build())
      .build()

    WorkManager.getInstance(this).enqueueUniquePeriodicWork(
      TimelineWork.TAG,
      ExistingPeriodicWorkPolicy.KEEP,
      pruneCacheWorker
    )

    AndroidLogcatLogger.installOnDebuggableApp(this, minPriority = LogPriority.VERBOSE)
  }
}