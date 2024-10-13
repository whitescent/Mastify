package com.github.whitescent.mastify

import android.app.Application
import com.tencent.mmkv.MMKV
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MastifyApp : Application() {
  override fun onCreate() {
    super.onCreate()
    MMKV.initialize(this)
  }
}
