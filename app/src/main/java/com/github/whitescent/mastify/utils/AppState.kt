@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package com.github.whitescent.mastify.utils

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Stable
class AppState(
  private val appContentPaddingTop: Dp,
  private val appContentPaddingBottom: Dp,
) {
  val appPaddingValues by lazy {
    PaddingValues(top = appContentPaddingTop, bottom = appContentPaddingBottom)
  }
  companion object {
    val saver = listSaver(
      save = { listOf(it.appContentPaddingTop.value, it.appContentPaddingBottom.value) },
      restore = { AppState(Dp(it[0] as Float), Dp(it[1] as Float)) }
    )
  }
}

@Composable
fun rememberAppState(topPadding: Dp = 0.dp, bottomPadding: Dp = 0.dp): AppState {
  return rememberSaveable(saver = AppState.saver) { AppState(topPadding, bottomPadding) }
}
