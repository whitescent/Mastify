package com.github.whitescent.mastify.screen.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions

object HomeTab : Tab {
  @Composable
  override fun Content() {
    val navigator = LocalNavigator.currentOrThrow
    Box(
      modifier = Modifier.fillMaxSize(),
      contentAlignment = Alignment.Center
    ) {
      Text(text = "home page")
    }
  }

  override val options: TabOptions
    @Composable
    get() {
      val title = "Home"
      val icon = rememberVectorPainter(Icons.Default.Home)
      return remember {
        TabOptions(
          index = 0u,
          title = title,
          icon = icon
        )
      }
    }
}