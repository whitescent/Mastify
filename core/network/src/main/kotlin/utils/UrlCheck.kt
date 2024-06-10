package com.github.whitescent.mastify.core.network.utils

import okhttp3.HttpUrl

fun isUrlCorrect(instance: String): Boolean {
  try {
    HttpUrl.Builder().host(instance).scheme("https").build()
  } catch (e: Exception) {
    return false
  }
  return true
}
