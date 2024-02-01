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

import com.github.whitescent.mastify.data.repository.StatusRepository
import com.github.whitescent.mastify.network.model.status.Status
import com.github.whitescent.mastify.paging.LoadResult
import com.github.whitescent.mastify.paging.PagingFactory
import com.github.whitescent.mastify.viewModel.ProfileKind
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class ProfilePagingFactory @Inject constructor(
  private val kind: ProfileKind,
  private val accountId: String,
  private val repository: StatusRepository
) : PagingFactory() {

  val list = MutableStateFlow(emptyList<Status>())

  override suspend fun append(pageSize: Int): LoadResult {
    val response = when (kind) {
      ProfileKind.Status -> repository.getAccountStatus(
        onlyMedia = false,
        excludeReplies = true,
        maxId = list.value.lastOrNull()?.id,
        limit = pageSize,
        accountId = accountId
      )
      ProfileKind.StatusWithReply -> repository.getAccountStatus(
        excludeReplies = false,
        maxId = list.value.lastOrNull()?.id,
        limit = pageSize,
        accountId = accountId
      )
      ProfileKind.StatusWithMedia -> repository.getAccountStatus(
        onlyMedia = true,
        maxId = list.value.lastOrNull()?.id,
        limit = pageSize,
        accountId = accountId
      )
    }.getOrThrow()
    list.emit((list.value + response).distinctBy { it.id })
    return LoadResult.Page(endReached = response.isEmpty())
  }

  override suspend fun refresh(pageSize: Int): LoadResult {
    val response = when (kind) {
      ProfileKind.Status -> repository.getAccountStatus(
        onlyMedia = false,
        excludeReplies = true,
        maxId = list.value.lastOrNull()?.id,
        limit = pageSize,
        accountId = accountId
      )
      ProfileKind.StatusWithReply -> repository.getAccountStatus(
        excludeReplies = false,
        maxId = list.value.lastOrNull()?.id,
        limit = pageSize,
        accountId = accountId
      )
      ProfileKind.StatusWithMedia -> repository.getAccountStatus(
        onlyMedia = true,
        maxId = list.value.lastOrNull()?.id,
        limit = pageSize,
        accountId = accountId
      )
    }.getOrThrow()
    list.emit(response.distinctBy { it.id })
    return LoadResult.Page(endReached = response.isEmpty())
  }
}
