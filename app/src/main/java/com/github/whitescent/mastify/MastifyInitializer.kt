package com.github.whitescent.mastify

import android.content.Context
import androidx.startup.Initializer
import com.tencent.mmkv.MMKV

class MastifyInitializer : Initializer<Unit> {
  override fun create(context: Context) {
    MMKV.initialize(context)
  }

  override fun dependencies(): List<Class<out Initializer<*>>> {
    return emptyList()
  }
}
