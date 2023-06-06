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
    timelineModel =
      mmkv.decodeParcelable("timeline_scroll_position", TimelineModel::class.java)
  }

  fun saveInstanceData(domain: String, clientId: String, clientSecret: String) {
    instance = InstanceModel(domain, clientId, clientSecret)
  }

  fun saveTimelineScrollPosition(index: Int, offset: Int) {
    mmkv.encode("timeline_scroll_position", TimelineModel(index, offset))
  }

}
