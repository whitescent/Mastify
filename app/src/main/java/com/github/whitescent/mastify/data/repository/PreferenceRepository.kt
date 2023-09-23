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

import com.github.whitescent.mastify.data.model.InstanceModel
import com.github.whitescent.mastify.data.model.TimelineModel
import com.tencent.mmkv.MMKV
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceRepository @Inject constructor() {

  private val mmkv = MMKV.defaultMMKV()

  // Cache the client ID and client secret during login
  var instance: InstanceModel? = null
    private set

  // Cache the last viewed timeline position of the user
  var timelineModel: TimelineModel? = null
    private set

  init {
    timelineModel = mmkv.decodeParcelable("timeline_scroll_position", TimelineModel::class.java)
  }

  fun saveInstanceData(domain: String, clientId: String, clientSecret: String) {
    instance = InstanceModel(domain, clientId, clientSecret)
  }

  fun saveTimelineScrollPosition(index: Int, offset: Int) {
    mmkv.encode("timeline_scroll_position", TimelineModel(index, offset))
  }
}
