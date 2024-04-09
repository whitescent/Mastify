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

package com.github.whitescent.mastify.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.github.whitescent.mastify.database.AppDatabase

class TimelineWork(
  context: Context,
  parameters: WorkerParameters,
  db: AppDatabase
) : CoroutineWorker(context, parameters) {

  private val timelineDao = db.timelineDao()
  private val accountDao = db.accountDao()

  override suspend fun doWork(): Result = try {
    val activeAccount = accountDao.getAccountList()
    activeAccount.forEach {
      Log.d("Mastify workmanager", "Pruning database using account ID: ${it.fullname}")
      timelineDao.cleanupOldTimeline(it.id, MAX_TIMELINE_SIZE)
      val timelineIndex = it.firstVisibleItemIndex
      if (timelineIndex > MAX_TIMELINE_SIZE) {
        accountDao.insertOrUpdate(it.copy(firstVisibleItemIndex = 0, offset = 0))
      }
    }
    Result.success()
  } catch (e: Exception) {
    e.printStackTrace()
    Result.failure()
  }

  companion object {
    const val TAG = "TimelineWork_periodic"
    private const val MAX_TIMELINE_SIZE = 150
  }
}

class TimelineWorkFactory(private val db: AppDatabase) : WorkerFactory() {
  override fun createWorker(
    appContext: Context,
    workerClassName: String,
    workerParameters: WorkerParameters
  ): ListenableWorker = TimelineWork(appContext, workerParameters, db)
}
