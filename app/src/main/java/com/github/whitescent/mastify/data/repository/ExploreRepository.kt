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

package com.github.whitescent.mastify.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import at.connyduck.calladapter.networkresult.fold
import com.github.whitescent.mastify.data.model.ui.StatusUiData
import com.github.whitescent.mastify.network.MastodonApi
import com.github.whitescent.mastify.network.model.search.SearchResult
import com.github.whitescent.mastify.paging.PublicTimelinePagingSource
import com.github.whitescent.mastify.paging.TrendingPagingSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExploreRepository @Inject constructor(
  private val api: MastodonApi
) {

  suspend fun getPreviewResultsForSearch(keyword: String): SearchPreviewResult {
    if (keyword.isBlank()) return SearchPreviewResult.Success(null)
    return api.searchSync(query = keyword, limit = 10).fold(
      {
        SearchPreviewResult.Success(it)
      },
      {
        it.printStackTrace()
        SearchPreviewResult.Failure(it)
      }
    )
  }

  fun getTrendingStatusPager(): Flow<PagingData<StatusUiData>> =
    Pager(
      config = PagingConfig(
        pageSize = 20,
        enablePlaceholders = false,
      ),
      pagingSourceFactory = {
        TrendingPagingSource(api = api)
      },
    ).flow

  fun getPublicTimelinePager(): Flow<PagingData<StatusUiData>> =
    Pager(
      config = PagingConfig(
        pageSize = 20,
        enablePlaceholders = false,
      ),
      pagingSourceFactory = {
        PublicTimelinePagingSource(api = api)
      },
    ).flow
}

sealed interface SearchPreviewResult {
  data class Success(val response: SearchResult?) : SearchPreviewResult
  data class Failure(val exception: Throwable) : SearchPreviewResult
}
