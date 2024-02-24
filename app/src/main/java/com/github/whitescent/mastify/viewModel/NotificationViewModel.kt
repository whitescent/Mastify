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
import com.github.whitescent.mastify.data.repository.NotificationRepository
import com.github.whitescent.mastify.mapper.toUiData
import com.github.whitescent.mastify.paging.Paginator
import com.github.whitescent.mastify.paging.factory.NotificationPagingFactory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
  private val repository: NotificationRepository
) : ViewModel() {

  private val notificationPagingFactory = NotificationPagingFactory(repository)

  val paginator = Paginator(
    pageSize = PAGE_SIZE,
    pagingFactory = notificationPagingFactory
  )

  val notifications = notificationPagingFactory.list
    .map { it.toUiData() }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.Eagerly,
      initialValue = emptyList()
    )

  companion object {
    const val PAGE_SIZE = 20
  }
}
