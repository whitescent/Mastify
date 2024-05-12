/*
 * Copyright 2024 WhiteScent
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

package com.github.whitescent.mastify.screen.other

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.Crossfade
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.github.fengdai.compose.media.Media
import com.github.fengdai.compose.media.ResizeMode
import com.github.fengdai.compose.media.ShowBuffering
import com.github.fengdai.compose.media.SurfaceType
import com.github.fengdai.compose.media.rememberMediaState
import com.github.panpf.zoomimage.ZoomImage
import com.github.whitescent.mastify.network.model.status.Status.Attachment
import com.github.whitescent.mastify.ui.component.CenterRow
import com.github.whitescent.mastify.ui.component.player.ExoPlayerLifecycleEvents
import com.github.whitescent.mastify.ui.component.player.rememberExoPlayerInstance
import com.github.whitescent.mastify.ui.component.player.rememberPositionState
import com.github.whitescent.mastify.ui.component.status.StatusMediaType
import com.github.whitescent.mastify.ui.theme.AppTheme
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph

@Destination<RootGraph>
@Composable
fun SharedTransitionScope.StatusMediaScreen(
  animatedVisibilityScope: AnimatedVisibilityScope,
  attachments: Array<Attachment>,
  targetMediaIndex: Int,
) {
  val pagerState = rememberPagerState(
    initialPage = targetMediaIndex,
    pageCount = { attachments.size }
  )
  val exoPlayer = rememberExoPlayerInstance()
  val mediaState = rememberMediaState(exoPlayer)
  val position = rememberPositionState(exoPlayer)

  ExoPlayerLifecycleEvents(exoPlayer)

  HorizontalPager(
    state = pagerState,
    modifier = Modifier
      .fillMaxSize()
      .background(Color.Black),
    pageContent = {
      val mediaItem = attachments[it]
      when (StatusMediaType.fromString(mediaItem.type)) {
        StatusMediaType.IMAGE -> {
          val painter = rememberAsyncImagePainter(
            model = ImageRequest.Builder(LocalContext.current)
              .data(mediaItem.url)
              .size(Size.ORIGINAL)
              .transformations()
              .build()
          )
          when (painter.state) {
            is AsyncImagePainter.State.Success -> {
              ZoomImage(
                painter = painter,
                contentDescription = null,
                modifier = Modifier
                  .fillMaxSize()
                  .sharedElement(
                    state = rememberSharedContentState(key = "image ${attachments[targetMediaIndex].url}"),
                    animatedVisibilityScope = animatedVisibilityScope
                  )
              )
            }
            is AsyncImagePainter.State.Loading -> {
              Box(Modifier.fillMaxSize()) {
                CircularProgressIndicator(
                  color = AppTheme.colors.accent,
                  modifier = Modifier.align(Alignment.Center)
                )
              }
            }
            else -> Unit
          }
        }
        StatusMediaType.VIDEO -> {
          Media(
            state = mediaState,
            resizeMode = ResizeMode.Fit,
            showBuffering = ShowBuffering.Always,
            buffering = {
              Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp)
              }
            },
            surfaceType = SurfaceType.TextureView,
          ) {
            Crossfade(mediaState.isControllerShowing, Modifier.fillMaxSize()) { showing ->
              when (showing) {
                true -> {
                  Box(
                    modifier = Modifier
                      .fillMaxSize()
                      .padding(24.dp),
                    contentAlignment = Alignment.BottomStart
                  ) {
                    CenterRow {
                      Text(
                        text = "position: $position",
                        color = Color.White,
                      )
                    }
                  }
                }
                else -> Unit
              }
            }
          }
          LaunchedEffect(Unit) {
            exoPlayer.run {
              repeatMode = Player.REPEAT_MODE_ONE
              setMediaItem(
                MediaItem.Builder().setMediaId(mediaItem.url).setUri(mediaItem.url).build()
              )
              prepare()
            }
          }
        }
        else -> Unit
      }
    },
  )
}
