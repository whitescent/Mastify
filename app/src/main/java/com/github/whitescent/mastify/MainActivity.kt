package com.github.whitescent.mastify

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.github.whitescent.mastify.data.repository.PreferenceRepository
import com.github.whitescent.mastify.ui.theme.MastifyTheme
import com.github.whitescent.mastify.utils.LocalSystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

  @Inject
  lateinit var preference: PreferenceRepository
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    WindowCompat.setDecorFitsSystemWindows(window, false)
    setContent {
      MastifyTheme {
        val systemUiController = rememberSystemUiController()
        val useDarkIcons = !isSystemInDarkTheme()
        val mainNavController = rememberNavController()
        val isLoggedIn = preference.anyAccountLoggedIn()
        SideEffect {
          systemUiController.setSystemBarsColor(
            color = Color.Transparent,
            darkIcons = useDarkIcons
          )
        }
        CompositionLocalProvider(LocalSystemUiController provides systemUiController) {
          NavHost(
            navController = mainNavController,
            startDestination = if (isLoggedIn) "app" else "login"
          ) {
            loginGraph(mainNavController)
            appGraph(mainNavController)
          }
        }
      }
    }
  }
}

typealias AppTheme = MaterialTheme
