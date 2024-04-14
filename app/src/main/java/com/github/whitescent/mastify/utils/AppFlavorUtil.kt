package com.github.whitescent.mastify.utils

import com.github.whitescent.BuildConfig

@Suppress("KotlinConstantConditions")
object AppFlavorUtil {
  const val IS_LIBRE_VARIANT = BuildConfig.FLAVOR == "libre"
}
