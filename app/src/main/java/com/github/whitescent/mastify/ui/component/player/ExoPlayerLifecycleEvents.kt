/*
 * Copyright 2023 WhiteScent
 *
 * This file is a part of Mastify.
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * Mastify is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Mastify; if not,
 * see <http://www.gnu.org/licenses>.
 */

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
