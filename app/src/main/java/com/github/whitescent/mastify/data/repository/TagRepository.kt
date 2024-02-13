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

import at.connyduck.calladapter.networkresult.getOrThrow
import com.github.whitescent.mastify.network.MastodonApi
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class TagRepository @Inject constructor(
  private val api: MastodonApi
) {
  suspend fun fetchHashtagTimeline(
    hashtag: String,
    any: List<String>? = null,
    maxId: String? = null,
    limit: Int? = null
  ) = api.hashtagTimeline(
    hashtag = hashtag,
    any = any,
    local = false,
    maxId = maxId,
    sinceId = null,
    limit = limit
  )

  suspend fun fetchHashtagInfo(name: String) = flow {
    emit(api.tag(name).getOrThrow())
  }

  suspend fun followHashtag(name: String, follow: Boolean) = flow {
    emit((if (follow) api.followTag(name) else api.unfollowTag(name)).getOrThrow())
  }
}
