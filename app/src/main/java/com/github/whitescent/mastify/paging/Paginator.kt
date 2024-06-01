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

package com.github.whitescent.mastify.paging

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.github.whitescent.mastify.paging.PageLoadState.NotLoading
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Simplified implementation of androidx Paging3
 * @param pageSize The number of items to be fetched per page
 * @param initRefresh Whether to initialize the refresh
 * @param pagingFactory The factory that provides the paging data, You need to write paging logic here,
 * including data sources for processing appendKey and paging list
 */
class Paginator(
  val pageSize: Int,
  coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main.immediate),
  private val initRefresh: Boolean = true,
  private val pagingFactory: PagingFactory,
) : Pager {

  var pagingLoadState by mutableStateOf<PageLoadState>(NotLoading(false))
    private set


  init {
    if (initRefresh) {
      coroutineScope.launch(Dispatchers.Main.immediate) {
        refresh()
      }
    }
  }

  override suspend fun append() {
    if (pagingLoadState == PageLoadState.Append) return
    pagingLoadState = PageLoadState.Append

    pagingLoadState = try {
      val job = withContext(Dispatchers.IO) {
        pagingFactory.append(pageSize)
      }
      delay(200)
      when (job) {
        is LoadResult.Page -> NotLoading(job.endReached)
        is LoadResult.Error -> PageLoadState.Error(job.throwable)
      }
    } catch (e: Exception) {
      e.printStackTrace()
      PageLoadState.Error(e)
    }
  }

  override suspend fun refresh() {
    if (pagingLoadState == PageLoadState.Refresh) return
    pagingLoadState = PageLoadState.Refresh

    pagingLoadState = try {
      val job = withContext(Dispatchers.IO) {
        pagingFactory.refresh(pageSize)
      }
      when (job) {
        is LoadResult.Page -> NotLoading(job.endReached)
        is LoadResult.Error -> PageLoadState.Error(job.throwable)
      }
    } catch (e: Exception) {
      e.printStackTrace()
      PageLoadState.Error(e)
    }
  }

}
