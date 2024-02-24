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

import com.github.whitescent.mastify.data.repository.NotificationRepository
import com.github.whitescent.mastify.network.model.notification.Notification
import com.github.whitescent.mastify.paging.LoadResult
import com.github.whitescent.mastify.paging.PagingFactory
import kotlinx.coroutines.flow.MutableStateFlow

class NotificationPagingFactory(
  private val repository: NotificationRepository,
) : PagingFactory() {

  val list = MutableStateFlow(emptyList<Notification>())

  override suspend fun append(pageSize: Int): LoadResult {
    val response = repository.getAccountNotifications(
      limit = pageSize,
      maxId = list.value.lastOrNull()?.id
    )
    list.emit(list.value + response)
    return LoadResult.Page(endReached = response.isEmpty())
  }

  override suspend fun refresh(pageSize: Int): LoadResult {
    val response = repository.getAccountNotifications(
      limit = pageSize,
      maxId = list.value.lastOrNull()?.id
    )
    list.emit(response)
    return LoadResult.Page(endReached = response.isEmpty() || response.size < pageSize)
  }
}
