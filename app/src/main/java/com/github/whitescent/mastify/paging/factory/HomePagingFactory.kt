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

package com.github.whitescent.mastify.paging.factory

import com.github.whitescent.mastify.data.repository.HomeRepository
import com.github.whitescent.mastify.database.AppDatabase
import com.github.whitescent.mastify.database.model.AccountEntity
import com.github.whitescent.mastify.network.model.status.Status
import com.github.whitescent.mastify.paging.LoadResult
import com.github.whitescent.mastify.paging.PagingFactory
import com.github.whitescent.mastify.viewModel.HomeNewStatusToastModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class HomePagingFactory @Inject constructor(
  db: AppDatabase,
  private val repository: HomeRepository,
) : PagingFactory() {

  private val accountDao = db.accountDao()
  private val timelineDao = db.timelineDao()

  private val coroutineScope = CoroutineScope(Dispatchers.IO)

  private lateinit var activeAccount: AccountEntity
  private lateinit var timeline: List<Status>

  private val refreshEvent = Channel<HomeNewStatusToastModel>()
  val refreshEventFlow = refreshEvent.receiveAsFlow()

  init {
    coroutineScope.launch {
      activeAccount = accountDao.getActiveAccount()!!
      timeline = timelineDao.getStatusList(activeAccount.id)
      timelineDao.getStatusListWithFlow(activeAccount.id)
        .collect {
          timeline = it
        }
    }
  }

  override suspend fun append(pageSize: Int): LoadResult {
    val response = repository.fetchTimeline(limit = pageSize, maxId = timeline.lastOrNull()?.id)
      .getOrThrow()
    repository.appendTimelineFromApi(response)
    return LoadResult.Page(endReached = response.isEmpty())
  }

  override suspend fun refresh(pageSize: Int): LoadResult {
    val response = repository.fetchTimeline(limit = pageSize).getOrThrow()
    val newStatusCount = response.filterNot {
      timeline.any { saved -> saved.id == it.id }
    }.size
    repository.refreshTimelineFromApi(timeline, response)
    refreshEvent.send(
      HomeNewStatusToastModel(
        showNewToastButton = newStatusCount != 0 && timeline.isNotEmpty(),
        newStatusCount = newStatusCount,
        showManyPost = !timeline.any { it.id == response.last().id }
      )
    )
    return LoadResult.Page(endReached = response.size < pageSize)
  }
}
