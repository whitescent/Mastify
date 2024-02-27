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

package com.github.whitescent.mastify.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.whitescent.mastify.data.repository.AccountRepository
import com.github.whitescent.mastify.data.repository.NotificationRepository
import com.github.whitescent.mastify.database.AppDatabase
import com.github.whitescent.mastify.paging.Paginator
import com.github.whitescent.mastify.paging.factory.NotificationPagingFactory
import com.github.whitescent.mastify.paging.factory.UnreadEvent
import com.github.whitescent.mastify.ui.component.status.StatusSnackbarType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
  db: AppDatabase,
  repository: NotificationRepository,
  private val accountRepository: AccountRepository
) : ViewModel() {

  private val notificationPagingFactory = NotificationPagingFactory(db, accountRepository, repository)

  private val snackBarChanel = Channel<StatusSnackbarType>(Channel.BUFFERED)
  val snackBarFlow = snackBarChanel.receiveAsFlow()

  private val unreadChannel = Channel<UnreadEvent>(Channel.BUFFERED)
  val unreadFlow = unreadChannel.receiveAsFlow()

  init {
    viewModelScope.launch {
      notificationPagingFactory.unreadFlow.collect {
        unreadChannel.send(it)
      }
    }
  }

  val paginator = Paginator(
    pageSize = PAGE_SIZE,
    pagingFactory = notificationPagingFactory
  )

  val notifications = notificationPagingFactory.list
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.Eagerly,
      initialValue = emptyList()
    )

  fun acceptFollowRequest(id: String) {
    viewModelScope.launch {
      accountRepository.acceptFollowRequest(id)
        .catch {
          it.printStackTrace()
          snackBarChanel.send(StatusSnackbarType.Error(it.localizedMessage))
        }
        .collect {}
    }
  }

  fun rejectFollowRequest(id: String) {
    viewModelScope.launch {
      accountRepository.rejectFollowRequest(id)
        .catch {
          it.printStackTrace()
          snackBarChanel.send(StatusSnackbarType.Error(it.localizedMessage))
        }
        .collect {}
    }
  }

  companion object {
    const val PAGE_SIZE = 20
  }
}
