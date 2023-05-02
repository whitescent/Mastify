package com.github.whitescent.mastify.ui.component.status

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.github.whitescent.mastify.AppTheme
import com.github.whitescent.mastify.network.model.response.account.MediaAttachments
import com.github.whitescent.mastify.ui.component.HeightSpacer
import com.github.whitescent.mastify.ui.component.WidthSpacer

val imageGridSpacing = 2.dp
const val defaultAspectRatio = 16f / 9f

@Composable
fun StatusMedia(
  mediaAttachments: List<MediaAttachments>,
  onClick: ((Int) -> Unit)? = null
) {
  val mediaCount = mediaAttachments.size
  val aspectRatio = if (mediaCount != 0) defaultAspectRatio else null
  Box(
    modifier = Modifier
      .let { m ->
        aspectRatio?.let { m.aspectRatio(it) } ?: m
      }
      .clip(AppTheme.shapes.medium)
  ) {
    when (mediaCount) {
      3 -> {
        Row {
          StatusMediaItem(
            media = mediaAttachments[0],
            modifier = Modifier.weight(1f).fillMaxSize(),
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
  if (MediaType.type(media.type) is MediaType.Image) {
    AsyncImage(
      model = media.url,
      contentDescription = null,
      contentScale = ContentScale.Crop,
      modifier = modifier
        .fillMaxSize()
        .clickable {
          onClick?.invoke()
        }
    )
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
