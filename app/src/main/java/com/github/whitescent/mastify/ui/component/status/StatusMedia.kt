package com.github.whitescent.mastify.ui.component.status

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.github.whitescent.R
import com.github.whitescent.mastify.network.model.status.Status.Attachment
import com.github.whitescent.mastify.ui.component.HeightSpacer
import com.github.whitescent.mastify.ui.component.WidthSpacer
import com.github.whitescent.mastify.utils.BlurTransformation

private val imageGridSpacing = 2.dp
private const val DefaultAspectRatio = 20f / 9f

@Composable
fun StatusMedia(
  sensitive: Boolean,
  spoilerText: String,
  attachments: List<Attachment>,
  modifier: Modifier = Modifier,
  onClick: ((Int) -> Unit)? = null
) {
  val mediaCount = attachments.size
  var mutableSensitive by rememberSaveable(sensitive) { mutableStateOf(sensitive) }
  Box(
    modifier = modifier
      .aspectRatio(DefaultAspectRatio)
      .clip(RoundedCornerShape(12.dp))
  ) {
    when (mediaCount) {
      3 -> {
        Row {
          StatusMediaItem(
            sensitive = mutableSensitive,
            media = attachments[0],
            modifier = Modifier
              .weight(1f)
              .fillMaxSize(),
            onClick = {
              if (!mutableSensitive) onClick?.invoke(0) else mutableSensitive = false
            }
          )
          WidthSpacer(value = imageGridSpacing)
          Column(modifier = Modifier.weight(1f)) {
            attachments.drop(1).forEachIndexed { index, it ->
              StatusMediaItem(
                sensitive = mutableSensitive,
                media = it,
                modifier = Modifier
                  .weight(1f)
                  .fillMaxSize(),
                onClick = {
                  if (!mutableSensitive) onClick?.invoke(index + 1) else mutableSensitive = false
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
              sensitive = mutableSensitive,
              media = it,
              modifier = Modifier,
              onClick = {
                if (!mutableSensitive) onClick?.invoke(index) else mutableSensitive = false
              }
            )
          }
        }
      }
    }
    if (mutableSensitive) {
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF3f3131),
        modifier = Modifier
          .align(Alignment.Center)
          .clickable { mutableSensitive = false },
        elevation = 12.dp
      ) {
        Text(
          text = spoilerText.ifEmpty { stringResource(id = R.string.sensitive_content) },
          modifier = Modifier.padding(12.dp),
          color = Color.White,
        )
      }
    }
  }
}

@Composable
fun StatusMediaItem(
  sensitive: Boolean,
  media: Attachment,
  modifier: Modifier = Modifier,
  onClick: (() -> Unit)? = null
) {
  val context = LocalContext.current
  when (MediaType.fromString(media.type)) {
    MediaType.IMAGE, MediaType.GIF -> {
      AsyncImage(
        model = ImageRequest.Builder(context)
          .data(media.url)
          .crossfade(true)
          .let {
            if (sensitive)
              it.transformations(BlurTransformation(context, radius = 25f, sampling = 3f))
            else it
          }
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
    MediaType.VIDEO -> {
      Box {
        AsyncImage(
          model = ImageRequest.Builder(context)
            .data(media.url)
            .crossfade(true)
            .let {
              if (sensitive)
                it.transformations(
                  BlurTransformation(LocalContext.current, radius = 25f, sampling = 3f)
                )
              else it
            }
            .build(),
          contentDescription = null,
          contentScale = ContentScale.Crop,
          modifier = modifier
            .fillMaxSize()
            .clickable {
              onClick?.invoke()
            },
        )
        Image(
          painter = painterResource(id = R.drawable.play_circle_fill),
          contentDescription = null,
          modifier = Modifier
            .size(48.dp)
            .align(Alignment.Center)
        )
      }
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
