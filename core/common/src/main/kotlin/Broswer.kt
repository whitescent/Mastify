package com.github.whitescent.mastify.core.common

import android.content.Context
import android.graphics.Color
import android.net.Uri
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent

fun launchChromeTabs(context: Context, uri: Uri) {
  val customTabBarColor = CustomTabColorSchemeParams.Builder()
    .setToolbarColor(Color.rgb(8, 27, 52)).build()
  val customTabsIntent = CustomTabsIntent.Builder()
    .setDefaultColorSchemeParams(customTabBarColor)
    .build()
  customTabsIntent.launchUrl(context, uri)
}
