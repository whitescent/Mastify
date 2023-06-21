package com.github.whitescent.mastify

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import com.github.whitescent.mastify.data.repository.AccountRepository
import com.github.whitescent.mastify.ui.theme.LocalMastifyColors
import com.github.whitescent.mastify.ui.theme.MastifyTheme
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.rememberAnimatedNavHostEngine
import com.ramcosta.composedestinations.navigation.dependency
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

  @Inject
  lateinit var accountRepository: AccountRepository

  @OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialNavigationApi::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    WindowCompat.setDecorFitsSystemWindows(window, false)
    super.onCreate(savedInstanceState)
    setContent {
      MastifyTheme {
        val systemUiController = rememberSystemUiController()
        val useDarkIcons = LocalMastifyColors.current.isLight
        val topNavController = rememberAnimatedNavController()
        val isLoggedIn = accountRepository.activeAccount != null
        SideEffect {
          systemUiController.setSystemBarsColor(
            color = Color.Transparent,
            darkIcons = useDarkIcons
          )
        }
        DestinationsNavHost(
          navGraph = NavGraphs.root,
          startRoute = if (isLoggedIn) NavGraphs.app else NavGraphs.login,
          dependenciesContainerBuilder = {
            // Provide primaryNavController to AppNavGraph to enable
            // navigation from a screen with BottomBar to a screen without BottomBar
            dependency(NavGraphs.app) { topNavController }
          },
          engine = rememberAnimatedNavHostEngine()
        )
      }
    }
  }
}
