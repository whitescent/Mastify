@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package com.github.whitescent.mastify.utils

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Stable
class AppState(
  private val appContentPaddingBottom: Dp,
) {

  var appPaddingValues by mutableStateOf(
    PaddingValues(bottom = appContentPaddingBottom)
  )
    private set

  fun setPaddingValues(paddingValues: PaddingValues) {
    appPaddingValues = paddingValues
  }

  companion object {
    val saver = listSaver(
      save = { listOf(it.appContentPaddingBottom.value) },
      restore = { AppState(Dp(it[0])) }
    )
  }
}

@Composable
fun rememberAppState(bottomPadding: Dp = 0.dp): AppState {
  return rememberSaveable(saver = AppState.saver) { AppState(bottomPadding) }
}
