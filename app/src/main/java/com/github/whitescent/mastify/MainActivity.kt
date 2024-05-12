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

package com.github.whitescent.mastify

import android.graphics.Color.TRANSPARENT
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.github.whitescent.mastify.ui.component.AppScaffold
import com.github.whitescent.mastify.ui.theme.MastifyTheme
import com.github.whitescent.mastify.viewModel.AppViewModel
import com.ramcosta.composedestinations.generated.NavGraphs
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
  private val appViewModel: AppViewModel by viewModels()
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    installSplashScreen().apply {
      setKeepOnScreenCondition { !appViewModel.prepared }
    }
    enableEdgeToEdge()
    setContent {
      MastifyTheme {
        val darkTheme = isSystemInDarkTheme()
        SideEffect {
          enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(TRANSPARENT, TRANSPARENT) { darkTheme },
            navigationBarStyle = SystemBarStyle.auto(TRANSPARENT, TRANSPARENT) { darkTheme },
          )
        }
        appViewModel.isLoggedIn?.let {
          AppScaffold(
            startRoute = if (it) NavGraphs.app else NavGraphs.login,
            viewModel = appViewModel
          )
        }
      }
    }
  }
}
