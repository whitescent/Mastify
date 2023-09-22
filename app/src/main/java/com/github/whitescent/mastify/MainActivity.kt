package com.github.whitescent.mastify

import android.graphics.Color.TRANSPARENT
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import com.github.whitescent.mastify.data.repository.AccountRepository
import com.github.whitescent.mastify.screen.NavGraphs
import com.github.whitescent.mastify.ui.component.AppScaffold
import com.github.whitescent.mastify.ui.theme.MastifyTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

  @Inject
  lateinit var accountRepository: AccountRepository

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MastifyTheme {
        val darkTheme = isSystemInDarkTheme()
        val isLoggedIn = accountRepository.activeAccount != null
        SideEffect {
          enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(TRANSPARENT, TRANSPARENT,) { darkTheme },
            navigationBarStyle = SystemBarStyle.auto(TRANSPARENT, TRANSPARENT,) { darkTheme },
          )
        }
        AppScaffold(
          startRoute = if (isLoggedIn) NavGraphs.app else NavGraphs.login
        )
      }
    }
  }
}
