package com.github.whitescent.mastify.ui.component.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun rememberPositionState(exoPlayer: ExoPlayer): Long {
  val scope = rememberCoroutineScope()
  var position by remember(exoPlayer) { mutableLongStateOf(exoPlayer.currentPosition) }
  LaunchedEffect(Unit) {
    scope.launch {
      while (true) {
        delay(200)
        position = exoPlayer.currentPosition
      }
    }
  }
  return position
}
