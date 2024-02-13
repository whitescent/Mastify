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

import com.github.whitescent.mastify.data.repository.SearchRepository
import com.github.whitescent.mastify.paging.LoadResult
import com.github.whitescent.mastify.paging.PagingFactory
import com.github.whitescent.mastify.viewModel.SearchType
import kotlinx.coroutines.flow.MutableStateFlow

@Suppress("UNCHECKED_CAST")
class SearchPagingFactory<T> (
  private val keyword: String,
  private val repository: SearchRepository,
  private val type: SearchType,
) : PagingFactory() {

  val list = MutableStateFlow(emptyList<T>())

  override suspend fun append(pageSize: Int): LoadResult {
    val searchResult = repository.fetchSearchResult(
      keyword = keyword,
      offset = list.value.size,
      limit = pageSize,
      type = type.toString()
    )
    val typeList = when (type) {
      SearchType.Status -> searchResult.statuses as List<T>
      SearchType.Account -> searchResult.accounts as List<T>
      SearchType.Hashtag -> searchResult.hashtags as List<T>
    }
    list.emit(list.value + typeList)
    return LoadResult.Page(typeList.isEmpty())
  }

  override suspend fun refresh(pageSize: Int): LoadResult {
    val searchResult = repository.fetchSearchResult(
      keyword = keyword,
      limit = pageSize,
      type = type.toString(),
      resolve = true
    )
    val typeList = when (type) {
      SearchType.Status -> searchResult.statuses as List<T>
      SearchType.Account -> searchResult.accounts as List<T>
      SearchType.Hashtag -> searchResult.hashtags as List<T>
    }
    list.emit(typeList)
    return LoadResult.Page(typeList.isEmpty() || typeList.size < pageSize)
  }
}
