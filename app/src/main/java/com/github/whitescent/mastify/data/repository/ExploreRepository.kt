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

package com.github.whitescent.mastify.data.repository

import at.connyduck.calladapter.networkresult.NetworkResult
import at.connyduck.calladapter.networkresult.fold
import at.connyduck.calladapter.networkresult.getOrThrow
import com.github.whitescent.mastify.network.MastodonApi
import com.github.whitescent.mastify.network.model.search.SearchResult
import javax.inject.Inject

class ExploreRepository @Inject constructor(
  private val api: MastodonApi
) {

  suspend fun getPreviewResultsForSearch(
    keyword: String,
    limit: Int? = null,
    offset: Int? = null
  ): NetworkResult<SearchResult?> {
    if (keyword.isBlank()) return NetworkResult.success(null)
    return api.searchSync(query = keyword, limit = limit, offset = offset).fold(
      {
        NetworkResult.success(it)
      },
      {
        it.printStackTrace()
        NetworkResult.failure(it)
      }
    )
  }

  suspend fun getTrending(limit: Int, offset: Int = 0) =
    api.trendingStatus(limit, offset).getOrThrow()

  suspend fun getPublicTimeline(maxId: String? = null, local: Boolean, limit: Int) =
    api.publicTimeline(local = local, maxId = maxId, limit = limit).getOrThrow()

  suspend fun getNews(limit: Int? = null, offset: Int = 0) =
    api.trendingNews(limit, offset).getOrThrow()
}
