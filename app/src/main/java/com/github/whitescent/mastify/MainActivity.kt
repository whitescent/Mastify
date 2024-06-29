package com.github.whitescent.mastify

import Mastify.core.codegen.AppDataPreferences
import android.graphics.Color.TRANSPARENT
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.github.whitescent.mastify.core.navigation.Route
import com.github.whitescent.mastify.core.ui.MastifyTheme
import com.github.whitescent.mastify.feature.foundation.foundationNavGraph
import com.github.whitescent.mastify.feature.login.loginNavGraph
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
  @Inject
  lateinit var appData: AppDataPreferences

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      MastifyTheme(activity = this) {
        val navController = rememberNavController()
        val darkTheme = isSystemInDarkTheme()
        SideEffect {
          enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(TRANSPARENT, TRANSPARENT) { darkTheme },
            navigationBarStyle = SystemBarStyle.auto(TRANSPARENT, TRANSPARENT) { darkTheme },
          )
        }
        NavHost(
          navController = navController,
          startDestination = when (appData.get().isLoggedIn) {
            true -> Route.Foundation
            false -> Route.Login
          }
        ) {
          loginNavGraph(navController)
          foundationNavGraph(navController)
        }
      }
    }
  }
}
