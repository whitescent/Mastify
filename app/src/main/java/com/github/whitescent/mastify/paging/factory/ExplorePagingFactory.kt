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

import com.github.whitescent.mastify.data.repository.ExploreRepository
import com.github.whitescent.mastify.network.model.status.Status
import com.github.whitescent.mastify.paging.LoadResult
import com.github.whitescent.mastify.paging.PagingFactory
import com.github.whitescent.mastify.utils.debug
import com.github.whitescent.mastify.viewModel.ExplorerKind
import kotlinx.coroutines.flow.MutableStateFlow

@Suppress("UNCHECKED_CAST")
class ExplorePagingFactory<T>(
  private val kind: ExplorerKind,
  private val repository: ExploreRepository,
) : PagingFactory() {

  val list = MutableStateFlow(emptyList<T>())

  override suspend fun append(pageSize: Int): LoadResult {
    val response = when (kind) {
      ExplorerKind.Trending -> repository.getTrending(pageSize, list.value.size)
      ExplorerKind.PublicTimeline -> repository.getPublicTimeline(
        maxId = (list.value as List<Status>).lastOrNull()?.id,
        local = true,
        limit = pageSize
      )
      ExplorerKind.News -> repository.getNews(pageSize, list.value.size)
    }
    list.emit(list.value + response as List<T>)
    debug { "response size ${response.size}" }
    return LoadResult.Page(endReached = response.isEmpty() || response.size < pageSize)
  }

  override suspend fun refresh(pageSize: Int): LoadResult {
    val response = when (kind) {
      ExplorerKind.Trending -> repository.getTrending(pageSize)
      ExplorerKind.PublicTimeline -> repository.getPublicTimeline(local = true, limit = pageSize)
      ExplorerKind.News -> repository.getNews(pageSize)
    }
    list.emit(response as List<T>)
    return LoadResult.Page(endReached = response.isEmpty() || response.size < pageSize)
  }
}
