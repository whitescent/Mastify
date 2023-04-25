package com.github.whitescent.mastify

import android.annotation.SuppressLint
import androidx.compose.material.Scaffold
import androidx.compose.material3.NavigationBar
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import com.github.whitescent.mastify.screen.home.HomeTab
import com.github.whitescent.mastify.ui.component.TabNavigationItem

object HomeScreen : Screen {
  @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
  @Composable
  override fun Content() {
    TabNavigator(HomeTab) {
      Scaffold(
        bottomBar = {
          NavigationBar {
            TabNavigationItem(HomeTab)
          }
        },
        content = { CurrentTab() }
      )
    }
  }
}