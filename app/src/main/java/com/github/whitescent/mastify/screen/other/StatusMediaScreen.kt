package com.github.whitescent.mastify.screen.other

import android.annotation.SuppressLint
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.github.whitescent.mastify.AppNavGraph
import com.github.whitescent.mastify.network.model.status.Status.Attachment
import com.github.whitescent.mastify.ui.component.CenterRow
import com.github.whitescent.mastify.ui.component.player.ExoPlayerLifecycleEvents
import com.github.whitescent.mastify.ui.component.player.rememberExoPlayerInstance
import com.github.whitescent.mastify.ui.component.player.rememberPositionState
import com.github.whitescent.mastify.ui.component.status.MediaType
import com.github.whitescent.mastify.ui.transitions.StatusMediaTransitions
import com.mxalbert.zoomable.Zoomable
import com.ramcosta.composedestinations.annotation.Destination

@OptIn(ExperimentalFoundationApi::class)
@AppNavGraph
@SuppressLint("UnsafeOptInUsageError")
@Destination(style = StatusMediaTransitions::class)
@Composable
fun StatusMediaScreen(
  attachments: Array<Attachment>,
  targetMediaIndex: Int,
) {
  var hideOverlay by remember { mutableStateOf(false) }
  val pagerState = rememberPagerState(
    initialPage = targetMediaIndex,
    pageCount = { attachments.size }
  )
  val exoPlayer = rememberExoPlayerInstance()
  val mediaState = rememberMediaState(exoPlayer)
  val position = rememberPositionState(exoPlayer)

  ExoPlayerLifecycleEvents(exoPlayer)
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color.Black)
  ) {
    HorizontalPager(
      state = pagerState,
      pageContent = {
        val mediaItem = attachments[it]
        Zoomable(
          modifier = Modifier.fillMaxSize(),
          onTap = {
            hideOverlay = !hideOverlay
          }
        ) {
          when (MediaType.fromString(mediaItem.type)) {
            MediaType.IMAGE -> {
              val painter = rememberAsyncImagePainter(
                model = ImageRequest.Builder(LocalContext.current)
                  .data(mediaItem.url)
                  .size(Size.ORIGINAL)
                  .transformations()
                  .build()
              )
              if (painter.state is AsyncImagePainter.State.Success) {
                val size = painter.intrinsicSize
                Image(
                  painter = painter,
                  contentDescription = null,
                  modifier = Modifier
                    .aspectRatio(size.width / size.height)
                    .fillMaxSize()
                )
              }
            }

            MediaType.VIDEO -> {
              Media(
                state = mediaState,
                resizeMode = ResizeMode.Fit,
                showBuffering = ShowBuffering.Always,
                buffering = {
                  Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp)
                  }
                },
                surfaceType = SurfaceType.SurfaceView,
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
        }
      },
    )
  }
}
