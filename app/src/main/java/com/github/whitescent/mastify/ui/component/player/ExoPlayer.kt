package com.github.whitescent.mastify.ui.component.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

@Composable
fun rememberExoPlayerInstance(): ExoPlayer {
  val context = LocalContext.current
  return remember {
    ExoPlayer.Builder(context).build().apply {
      repeatMode = Player.REPEAT_MODE_ALL
      volume = 1f
    }
  }
}
