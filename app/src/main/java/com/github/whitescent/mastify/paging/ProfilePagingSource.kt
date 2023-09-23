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
import at.connyduck.calladapter.networkresult.fold
import com.github.whitescent.mastify.data.model.ui.StatusUiData
import com.github.whitescent.mastify.mapper.status.toUiData
import com.github.whitescent.mastify.network.MastodonApi
import com.github.whitescent.mastify.network.model.status.Status
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class ProfilePagingSource @Inject constructor(
  val onlyMedia: Boolean? = null,
  val excludeReplies: Boolean? = null,
  val accountId: String,
  private val api: MastodonApi,
) : PagingSource<String, StatusUiData>() {

  private var nextPageId: String? = null

  override fun getRefreshKey(state: PagingState<String, StatusUiData>): String? {
    return if (nextPageId == null) null else nextPageId
  }

  override suspend fun load(params: LoadParams<String>): LoadResult<String, StatusUiData> {
    return try {
      val data = api.accountStatuses(
        accountId = accountId,
        maxId = if (nextPageId != null) nextPageId else null,
        excludeReplies = excludeReplies,
        onlyMedia = onlyMedia
      ).body()!!
      var temp: MutableList<Status> = mutableListOf()
      // If we need to request a status list containing replies,
      // we need to obtain the requested status
      // for good ux !
      excludeReplies?.let {
        if (!it) {
          temp = data.toMutableList()
          data.forEach { status ->
            if (status.isInReplyTo) {
              api.status(status.inReplyToId!!).fold(
                { repliedStatus ->
                  temp.add(temp.indexOf(status), repliedStatus)
                },
                { e ->
                  e.printStackTrace()
                }
              )
            }
          }
        }
      }
      val result = if (temp.size == 0) data.toUiData() else temp.toUiData()
      LoadResult.Page(
        data = result,
        prevKey = nextPageId,
        nextKey = if (data.isEmpty()) null else data.last().id
      ).also {
        it.nextKey?.let { id -> nextPageId = id }
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
