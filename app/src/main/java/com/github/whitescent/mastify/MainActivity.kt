package com.github.whitescent.mastify

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import com.github.whitescent.mastify.data.repository.AccountRepository
import com.github.whitescent.mastify.screen.NavGraphs
import com.github.whitescent.mastify.ui.component.AppScaffold
import com.github.whitescent.mastify.ui.theme.LocalMastifyColors
import com.github.whitescent.mastify.ui.theme.MastifyTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

  @Inject
  lateinit var accountRepository: AccountRepository

  override fun onCreate(savedInstanceState: Bundle?) {
    WindowCompat.setDecorFitsSystemWindows(window, false)
    super.onCreate(savedInstanceState)
    setContent {
      MastifyTheme {
        val systemUiController = rememberSystemUiController()
        val useDarkIcons = LocalMastifyColors.current.isLight
        val isLoggedIn = accountRepository.activeAccount != null
        SideEffect {
          systemUiController.setSystemBarsColor(
            color = Color.Transparent,
            darkIcons = useDarkIcons
          )
        }
        AppScaffold(
          startRoute = if (isLoggedIn) NavGraphs.app else NavGraphs.login,
          systemUiController = systemUiController
        )
      }
    }
  }
}
