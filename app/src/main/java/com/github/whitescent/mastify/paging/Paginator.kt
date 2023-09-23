/*
 * Copyright 2023 WhiteScent
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

package com.github.whitescent.mastify.paging

class Paginator<Key, Item>(
  initialKey: Key,
  private val refreshKey: Key,
  private inline val onLoadUpdated: (LoadState) -> Unit,
  private inline val onRequest: suspend (nextKey: Key) -> Result<List<Item>>,
  private inline val getNextKey: suspend (List<Item>, loadState: LoadState) -> Key,
  private inline val onError: suspend (Throwable?) -> Unit,
  private inline val onAppend: suspend (items: List<Item>) -> Unit,
  private inline val onRefresh: suspend (items: List<Item>) -> Unit
) : PaginatorInterface<Key, Item> {

  private var currentKey = initialKey
  private var loadState = LoadState.NotLoading

  override suspend fun append() {
    if (loadState == LoadState.Append) return
    loadState = LoadState.Append
    onLoadUpdated(loadState)
    try {
      val result = onRequest(currentKey).getOrElse {
        onError(it)
        loadState = LoadState.Error
        onLoadUpdated(loadState)
        return
      }
      currentKey = getNextKey(result, loadState)
      onAppend(result)
      loadState = LoadState.NotLoading
      onLoadUpdated(loadState)
    } catch (e: Exception) {
      onError(e)
      loadState = LoadState.Error
      onLoadUpdated(loadState)
      return
    }
  }

  override suspend fun refresh() {
    if (loadState == LoadState.Refresh) return
    loadState = LoadState.Refresh
    onLoadUpdated(loadState)
    try {
      val result = onRequest(refreshKey).getOrElse {
        onError(it)
        loadState = LoadState.Error
        onLoadUpdated(loadState)
        return
      }
      currentKey = getNextKey(result, loadState)
      onRefresh(result)
      loadState = LoadState.NotLoading
      onLoadUpdated(loadState)
    } catch (e: Exception) {
      onError(e)
      loadState = LoadState.Error
      onLoadUpdated(loadState)
      return
    }
  }
}

enum class LoadState {
  Refresh, Append, Error, NotLoading
}
