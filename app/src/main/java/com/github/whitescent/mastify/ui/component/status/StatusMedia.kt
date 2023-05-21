package com.github.whitescent.mastify.ui.component.status

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import com.github.whitescent.R
import com.github.whitescent.mastify.network.model.response.account.MediaAttachments
import com.github.whitescent.mastify.ui.component.HeightSpacer
import com.github.whitescent.mastify.ui.component.WidthSpacer

val imageGridSpacing = 2.dp
const val defaultAspectRatio = 20f / 9f

@Composable
fun StatusMedia(
  mediaAttachments: List<MediaAttachments>,
  onClick: ((Int) -> Unit)? = null
) {
  val mediaCount = mediaAttachments.size
  Box(
    modifier = Modifier
      .aspectRatio(defaultAspectRatio)
      .clip(RoundedCornerShape(12.dp))
  ) {
    when (mediaCount) {
      3 -> {
        Row {
          StatusMediaItem(
            media = mediaAttachments[0],
            modifier = Modifier
              .weight(1f)
              .fillMaxSize(),
            onClick = {
              onClick?.invoke(0)
            }
          )
          WidthSpacer(value = imageGridSpacing)
          Column(modifier = Modifier.weight(1f)) {
            mediaAttachments.drop(1).forEachIndexed { index, it ->
              StatusMediaItem(
                media = it,
                modifier = Modifier
                  .weight(1f)
                  .fillMaxSize(),
                onClick = {
                  onClick?.invoke(index + 1)
                }
              )
              if (it != mediaAttachments.last()) {
                HeightSpacer(value = imageGridSpacing)
              }
            }
          }
        }
      }
      else -> {
        StatusMediaGrid(spacing = 2.dp) {
          mediaAttachments.forEachIndexed { index, it ->
            StatusMediaItem(
              media = it,
              modifier = Modifier,
              onClick = {
                onClick?.invoke(index)
              }
            )
          }
        }
      }
    }
  }
}

@Composable
fun StatusMediaItem(
  media: MediaAttachments,
  modifier: Modifier = Modifier,
  onClick: (() -> Unit)? = null
) {
  val context = LocalContext.current
  when (MediaType.type(media.type)) {
    is MediaType.Image -> {
      AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
          .data(media.url)
          .crossfade(true)
          .build(),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier
          .fillMaxSize()
          .clickable {
            onClick?.invoke()
          }
      )
    }
    is MediaType.Video -> {
      Box {
        val painter = rememberAsyncImagePainter(
          model = ImageRequest.Builder(LocalContext.current)
            .data(media.url)
            .crossfade(true)
            .build(),
          imageLoader = ImageLoader.Builder(context)
            .components {
              add(VideoFrameDecoder.Factory())
            }
            .build(),
        )
        Image(
         painter = painter,
          contentDescription = null,
          contentScale = ContentScale.Crop,
          modifier = modifier
            .fillMaxSize()
            .clickable {
              onClick?.invoke()
            },
        )
        if (painter.state is AsyncImagePainter.State.Success) {
          Image(
            painter = painterResource(id = R.drawable.play_circle_fill),
            contentDescription = null,
            modifier = Modifier.size(48.dp).align(Alignment.Center)
          )
        }
      }
    }
    else -> Unit
  }
}

sealed class MediaType {

  object Image: MediaType()
  object Video: MediaType()
  object Null: MediaType()

  companion object {
    fun type(type: String): MediaType {
      return when(type) {
        "image" -> Image
        "video" -> Video
        else -> Null
      }
    }
  }

}
