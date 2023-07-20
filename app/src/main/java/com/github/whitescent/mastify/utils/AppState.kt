package com.github.whitescent.mastify.utils

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp

@Stable
class AppState {
  var appContentPaddingValues = PaddingValues(0.dp)
}

@Composable
fun rememberAppState(): AppState {
  return remember { AppState() }
}
