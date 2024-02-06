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
import androidx.room.withTransaction
import com.github.whitescent.mastify.database.AppDatabase
import com.github.whitescent.mastify.mapper.toEntity
import com.github.whitescent.mastify.network.MastodonApi
import com.github.whitescent.mastify.network.model.status.Status
import com.github.whitescent.mastify.utils.getOrThrow
import com.github.whitescent.mastify.utils.getServerErrorMessage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import javax.inject.Inject

class HomeRepository @Inject constructor(
  @ApplicationContext private val context: Context,
  private val api: MastodonApi,
  private val db: AppDatabase,
  private val accountRepository: AccountRepository
) {

  private val accountDao = db.accountDao()
  private val timelineDao = db.timelineDao()

  suspend fun refreshTimelineFromApi(
    accountId: Long,
    oldItems: List<Status>,
    newItems: List<Status>
  ) {
    if (newItems.isEmpty()) return
    if (oldItems.size < FETCH_NUMBER) saveLatestTimelineToDb(newItems, accountId)
    else {
      val lastStatusOfNewItems = newItems.last()
      // If the db contains the last status returned by the API,
      // we need to splice the new data with the data in the database
      if (oldItems.any { it.id == lastStatusOfNewItems.id }) {
        val indexInSavedList = oldItems.indexOfFirst {
          it.id == lastStatusOfNewItems.id
        } + 1
        // get a list in the database that is not in the API
        val statusListAfterFetchNumber = oldItems.subList(indexInSavedList, oldItems.size)

        // Keep all Status with hasUnloadedStatus because they haven't been clicked by the user yet
        val statusListWithUnloaded =
          (oldItems - statusListAfterFetchNumber.toSet()).filter { it.hasUnloadedStatus }
        val statusListBeforeFetchNumber = newItems.toMutableList().apply {
          replaceAll { new ->
            statusListWithUnloaded.find { saved -> saved.id == new.id } ?: new
          }
        }

        // Update the timeline to the database by splicing the latest data
        // from the API with the latest data from the database.
        saveLatestTimelineToDb(statusListBeforeFetchNumber + statusListAfterFetchNumber, accountId)
      } else {
        // If the last currentStatus returned by the API cannot be found in the db,
        // This means that the number of statuses in the user's timeline exceeds
        // the number of statuses in a single API request,
        // and we need to display 'Load More' button
        val newStatusList = newItems.toMutableList()
        newStatusList[newStatusList.lastIndex] =
          newStatusList[newStatusList.lastIndex].copy(hasUnloadedStatus = true)
        newItems.forEach {
          // Here we need to consider whether oldItems already contains the hasUnloadedStatus
          // If so, we need to add a new list on top of oldItems instead of overwriting it
          // for more information, check TimelineListTest.kt case 7
          if (oldItems.any { saved -> saved.id == it.id }) {
            val removeIndex = oldItems.indexOfFirst { it.hasUnloadedStatus }.let {
              if (it == -1) 0 else it + 1
            }
            saveLatestTimelineToDb(newStatusList + oldItems.subList(removeIndex, oldItems.size), accountId)
            return
          }
        }
        saveLatestTimelineToDb(newStatusList + oldItems, accountId)
      }
    }
  }

  suspend fun appendTimelineFromApi(newItems: List<Status>) {
    if (newItems.isEmpty()) return
    db.withTransaction {
      val activeAccount = accountDao.getActiveAccount()!!
      timelineDao.insertOrUpdate(newItems.toEntity(activeAccount.id))
    }
  }

  /**
   * Find the Status with startId from the list,
   * and request data from the API before this Status
   */
  suspend fun fillMissingStatusesAround(startId: String? = null, currentList: List<Status>) {
    val tempList = currentList.toMutableList()
    val insertIndex = tempList.indexOfFirst { it.id == startId }
    fetchTimelineFlow(startId)
      .catch {
        Toast.makeText(context, it.localizedMessage, Toast.LENGTH_LONG).show()
      }
      .collect { response ->
        val list = response.toMutableList()
        // If the data in this request contains the first data from the database,
        // we need connect the data in this request with the data in the database
        if (tempList.any { it.id == list.last().id }) {
          tempList[insertIndex] = tempList[insertIndex].copy(hasUnloadedStatus = false)
          tempList.addAll(
            index = insertIndex + 1,
            elements = list.filterNot {
              tempList.any { saved -> saved.id == it.id }
            }
          )
        } else {
          // If the data in this request doesn't contain the first data from the database,
          // we need to add it and mark the last post as Unloaded
          list[list.lastIndex] = list[list.lastIndex].copy(hasUnloadedStatus = true)
          tempList[insertIndex] = tempList[insertIndex].copy(hasUnloadedStatus = false)
          tempList.addAll(insertIndex + 1, list)
        }
        saveLatestTimelineToDb(tempList, accountDao.getActiveAccount()!!.id)
      }
  }

  suspend fun fetchTimeline(maxId: String? = null, limit: Int = FETCH_NUMBER): Result<List<Status>> {
    val response = api.homeTimeline(maxId = maxId, limit = limit)
    return if (response.isSuccessful && !response.body().isNullOrEmpty()) {
      val body = response.body()!!
      Result.success(body)
    } else {
      val error = HttpException(response)
      val errorMessage = error.getServerErrorMessage()
      if (errorMessage == null) {
        Result.failure(error)
      } else {
        Result.failure(Throwable(errorMessage))
      }
    }
  }

  suspend fun saveLastViewedTimelineOffset(index: Int, offset: Int) {
    val activeAccount = accountDao.getActiveAccount()!!
    db.withTransaction {
      accountRepository.updateActiveAccount(
        activeAccount.copy(firstVisibleItemIndex = index, offset = offset)
      )
    }
  }

  private suspend fun saveLatestTimelineToDb(statuses: List<Status>, accountId: Long) =
    timelineDao.cleanAndReinsert(statuses.map { it.toEntity(accountId) }, accountId)

  private suspend fun fetchTimelineFlow(maxId: String?, limit: Int = FETCH_NUMBER) = flow {
    emit(api.homeTimeline(maxId, limit = limit).getOrThrow())
  }

  companion object {
    const val FETCH_NUMBER = 40
  }
}
