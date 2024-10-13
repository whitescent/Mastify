package com.github.whitescent.mastify.core.model

import com.meowool.mmkv.ktx.Preferences

@Preferences
data class AppData(
  val instanceUrl: String? = null,
  val token: String? = null
) {
  inline val isLoggedIn: Boolean get() = token != null
}
