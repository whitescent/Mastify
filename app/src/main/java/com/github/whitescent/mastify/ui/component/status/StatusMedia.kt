package com.github.whitescent.mastify.ui.component.status

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.github.whitescent.mastify.network.model.status.Status.Attachment
import com.github.whitescent.mastify.ui.component.AsyncBlurImage
import com.github.whitescent.mastify.ui.component.HeightSpacer
import com.github.whitescent.mastify.ui.component.WidthSpacer
import kotlinx.collections.immutable.ImmutableList

private val imageGridSpacing = 2.dp
private const val DefaultAspectRatio = 20f / 9f

@Composable
fun StatusMedia(
  attachments: ImmutableList<Attachment>,
  modifier: Modifier = Modifier,
  onClick: ((Int) -> Unit)? = null
) {
  val mediaCount by remember(attachments.size) { mutableIntStateOf(attachments.size) }
  Box(
    modifier = modifier
      .aspectRatio(DefaultAspectRatio)
      .clip(RoundedCornerShape(12.dp))
  ) {
    when (mediaCount) {
      3 -> {
        Row {
          StatusMediaItem(
            media = attachments[0],
            modifier = Modifier
              .weight(1f)
              .fillMaxSize(),
            onClick = {
              onClick?.invoke(0)
            }
          )
          WidthSpacer(value = imageGridSpacing)
          Column(modifier = Modifier.weight(1f)) {
            attachments.drop(1).forEachIndexed { index, it ->
              StatusMediaItem(
                media = it,
                modifier = Modifier
                  .weight(1f)
                  .fillMaxSize(),
                onClick = {
                  onClick?.invoke(index + 1)
                }
              )
              if (it != attachments.last()) {
                HeightSpacer(value = imageGridSpacing)
              }
            }
          }
        }
      }
      else -> {
        StatusMediaGrid(spacing = 2.dp) {
          attachments.forEachIndexed { index, it ->
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
  media: Attachment,
  modifier: Modifier = Modifier,
  onClick: (() -> Unit)? = null
) {
  when (MediaType.fromString(media.type)) {
    MediaType.IMAGE, MediaType.GIF -> {
      AsyncBlurImage(
        url = media.url,
        blurHash = media.blurhash ?: "",
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier
          .fillMaxSize()
          .clickable {
            onClick?.invoke()
          }
      )
    }
    else -> Unit
  }
}

enum class MediaType {
  IMAGE, VIDEO, GIF, NULL;
  companion object {
    fun fromString(type: String): MediaType {
      return when (type) {
        "image" -> IMAGE
        "video" -> VIDEO
        "gifv" -> GIF
        else -> NULL
      }
    }
  }
}
