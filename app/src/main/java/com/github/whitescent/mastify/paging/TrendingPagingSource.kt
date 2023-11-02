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

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.github.whitescent.mastify.data.model.ui.StatusUiData
import com.github.whitescent.mastify.mapper.status.toUiData
import com.github.whitescent.mastify.network.MastodonApi
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class TrendingPagingSource @Inject constructor(
  private val api: MastodonApi,
) : PagingSource<Int, StatusUiData>() {

  private var nextPage: Int = 0

  override fun getRefreshKey(state: PagingState<Int, StatusUiData>): Int = 0

  override suspend fun load(params: LoadParams<Int>): LoadResult<Int, StatusUiData> {
    return try {
      val data = api.trendingStatus(offset = nextPage).body()!!.toUiData()
      LoadResult.Page(
        data = data,
        prevKey = null,
        nextKey = if (data.isEmpty()) null else nextPage
      ).also {
        it.nextKey?.let {
          nextPage += data.size
        }
      }
    } catch (exception: IOException) {
      exception.printStackTrace()
      return LoadResult.Error(exception)
    } catch (exception: HttpException) {
      exception.printStackTrace()
      return LoadResult.Error(exception)
    }
  }
}
