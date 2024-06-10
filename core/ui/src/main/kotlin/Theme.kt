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

package com.github.whitescent.mastify.core.ui

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.github.whitescent.mastify.core.common.compose.LocalActivity

@Composable
fun MastifyTheme(
  isDark: Boolean = isSystemInDarkTheme(),
  activity: ComponentActivity? = null,
  content: @Composable () -> Unit
) {
  val colorScheme = if (isDark) MastifyColorScheme.Dark else MastifyColorScheme.Light
  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as Activity).window
      window.statusBarColor = colorScheme.primaryContent.toArgb()
      WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = isDark
    }
  }
  CompositionLocalProvider(
    LocalActivity provides activity,
    LocalColorScheme provides colorScheme,
    LocalIndication provides ripple()
  ) {
    content.invoke()
  }
}
