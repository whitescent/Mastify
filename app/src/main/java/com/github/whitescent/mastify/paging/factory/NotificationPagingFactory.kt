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

import com.github.whitescent.mastify.data.model.ui.NotificationUiData
import com.github.whitescent.mastify.data.repository.AccountRepository
import com.github.whitescent.mastify.data.repository.NotificationRepository
import com.github.whitescent.mastify.database.AppDatabase
import com.github.whitescent.mastify.mapper.toUiData
import com.github.whitescent.mastify.paging.LoadResult
import com.github.whitescent.mastify.paging.PagingFactory
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

class NotificationPagingFactory(
  db: AppDatabase,
  private val accountRepository: AccountRepository,
  private val repository: NotificationRepository,
) : PagingFactory() {

  private val accountDao = db.accountDao()

  val list = MutableStateFlow(emptyList<NotificationUiData>())

  private val unreadChannel = Channel<UnreadEvent>(Channel.BUFFERED)
  val unreadFlow = unreadChannel.receiveAsFlow()

  override suspend fun append(pageSize: Int): LoadResult {
    val activeAccount = accountDao.getActiveAccount()!!
    val response = repository.getAccountNotifications(
      limit = pageSize,
      maxId = list.value.lastOrNull()?.id
    ).map { it.toUiData(activeAccount.lastNotificationId) }
    list.emit(list.value + response)
    unreadChannel.send(
      UnreadEvent.Append(getUnreadCount(response, activeAccount.lastNotificationId!!))
    )
    return LoadResult.Page(endReached = response.isEmpty())
  }

  override suspend fun refresh(pageSize: Int): LoadResult {
    val activeAccount = accountDao.getActiveAccount()!!
    val response = repository.getAccountNotifications(limit = pageSize)
      .map { it.toUiData(activeAccount.lastNotificationId) }
    if (response.isNotEmpty()) {
      if (activeAccount.lastNotificationId != null) {
        unreadChannel.send(
          UnreadEvent.Refresh(getUnreadCount(response, activeAccount.lastNotificationId))
        )
      }
      accountRepository.updateActiveAccount(
        account = activeAccount.copy(lastNotificationId = response.first().id)
      )
    }
    list.emit(response)
    return LoadResult.Page(endReached = response.isEmpty() || response.size < pageSize)
  }
}

sealed class UnreadEvent {
  data class Refresh(val count: Int) : UnreadEvent()
  data class Append(val count: Int) : UnreadEvent()
  data object Dismiss : UnreadEvent()
  data object DismissAll : UnreadEvent()
}

private fun getUnreadCount(list: List<NotificationUiData>, lastId: String): Int =
  list.count { it.id > lastId }
