package com.github.whitescent.mastify.ui.component.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.exoplayer.ExoPlayer

@Composable
fun ExoPlayerLifecycleEvents(
  exoPlayer: ExoPlayer,
) {
  val lifecycle = LocalLifecycleOwner.current
  DisposableEffect(exoPlayer) {
    val lifecycleObserver = LifecycleEventObserver { _, event ->
      when (event) {
        Lifecycle.Event.ON_START -> exoPlayer.play()
        Lifecycle.Event.ON_STOP -> exoPlayer.pause()
        else -> {}
      }
    }

    lifecycle.lifecycle.addObserver(lifecycleObserver)

    onDispose {
      lifecycle.lifecycle.removeObserver(lifecycleObserver)
      exoPlayer.release()
    }
  }
}
