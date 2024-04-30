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

package com.github.whitescent.mastify.ui.component.status

import android.net.Uri
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.whitescent.R
import com.github.whitescent.mastify.network.model.status.Status.Attachment
import com.github.whitescent.mastify.ui.component.AsyncBlurImage
import com.github.whitescent.mastify.ui.component.CenterRow
import com.github.whitescent.mastify.ui.component.CircleShapeAsyncImage
import com.github.whitescent.mastify.ui.component.HeightSpacer
import com.github.whitescent.mastify.ui.component.LocalAnimatedVisibilityScope
import com.github.whitescent.mastify.ui.component.LocalSharedTransitionScope
import com.github.whitescent.mastify.ui.component.WidthSpacer
import com.github.whitescent.mastify.ui.component.foundation.Text
import com.github.whitescent.mastify.ui.component.player.rememberExoPlayerInstance
import com.github.whitescent.mastify.ui.theme.AppTheme
import com.github.whitescent.mastify.utils.launchCustomChromeTab
import kotlinx.collections.immutable.ImmutableList

private val ImageGridSpacing = 2.dp
private const val DefaultAspectRatio = 270f / 162f
private val DefaultMaxHeight = 400.dp

@Composable
fun StatusMedia(
  avatar: String,
  attachments: ImmutableList<Attachment>,
  modifier: Modifier = Modifier,
  onClick: ((Int) -> Unit)? = null
) {
  val mediaCount by remember(attachments.size) { mutableIntStateOf(attachments.size) }
  val boxAspectRatio = remember(mediaCount) {
    when (mediaCount) {
      in 2..4 -> DefaultAspectRatio
      1 -> attachments.first().meta?.original?.let { meta ->
        when (StatusMediaType.fromString(attachments.first().type)) {
          StatusMediaType.VIDEO -> DefaultAspectRatio
          StatusMediaType.UNKNOWN -> null
          StatusMediaType.IMAGE -> (meta.width.toFloat() / meta.height.toFloat()).let {
            if (it.isNaN()) DefaultAspectRatio else it
          }
        }
      }
      else -> null
    }
  }
  Box(
    modifier = modifier
      .let {
        if (mediaCount == 1) {
          it.heightIn(max = DefaultMaxHeight)
        } else {
          it
        }
      }
      .let {
        boxAspectRatio?.let { ratio ->
          it.aspectRatio(ratio)
        } ?: it
      }
      .border(0.4.dp, Color.Gray, AppTheme.shape.normal)
      .clip(AppTheme.shape.normal)
  ) {
    when (mediaCount) {
      1 -> {
        StatusMediaItem(
          avatar = avatar,
          media = attachments[0],
          onClick = {
            onClick?.invoke(0)
          }
        )
      }
      3 -> {
        Row {
          StatusMediaItem(
            avatar = avatar,
            media = attachments[0],
            modifier = Modifier
              .weight(1f)
              .fillMaxSize(),
            onClick = {
              onClick?.invoke(0)
            }
          )
          WidthSpacer(value = ImageGridSpacing)
          Column(modifier = Modifier.weight(1f)) {
            attachments.drop(1).forEachIndexed { index, media ->
              StatusMediaItem(
                avatar = avatar,
                media = media,
                modifier = Modifier
                  .weight(1f)
                  .fillMaxSize(),
                onClick = {
                  onClick?.invoke(index + 1)
                }
              )
              if (media != attachments.last()) {
                HeightSpacer(value = ImageGridSpacing)
              }
            }
          }
        }
      }
      else -> {
        StatusMediaGrid(spacing = 2.dp) {
          attachments.forEachIndexed { index, media ->
            StatusMediaItem(
              avatar = avatar,
              media = media,
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
private fun StatusMediaItem(
  avatar: String,
  media: Attachment,
  modifier: Modifier = Modifier,
  onClick: (() -> Unit)? = null
) {
  val sharedTransitionScope = LocalSharedTransitionScope.current
  val animatedVisibilityScope = LocalAnimatedVisibilityScope.current

  sharedTransitionScope.apply {
    when (StatusMediaType.fromString(media.type)) {
      StatusMediaType.IMAGE -> {
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
            .sharedElement(
              state = rememberSharedContentState(key = "image ${media.url}"),
              animatedVisibilityScope = animatedVisibilityScope,
              placeHolderSize = SharedTransitionScope.PlaceHolderSize.contentSize
            )
        )
      }
      StatusMediaType.VIDEO -> {
        rememberExoPlayerInstance()
        // show thumbnail
        Box(
          modifier = modifier
            .fillMaxSize()
            .let {
              if (media.type == "audio") {
                it.background(AppTheme.colors.accent)
              } else it
            }
            .clickable {
              onClick?.invoke()
            }
        ) {
          when (media.type) {
            "video" -> {
              AsyncBlurImage(
                url = media.previewUrl,
                blurHash = media.blurhash ?: "",
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
              )
            }
            "audio" -> {
              CenterRow(
                modifier = Modifier
                  .padding(12.dp)
                  .fillMaxWidth()
                  .align(Alignment.TopStart)
              ) {
                Box(Modifier.weight(1f)) {
                  CircleShapeAsyncImage(
                    model = avatar,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape
                  )
                }
                Icon(
                  painter = painterResource(id = R.drawable.headphones_bold),
                  contentDescription = null,
                  modifier = Modifier.size(24.dp),
                  tint = Color.White
                )
              }
            }
          }
          Image(
            painter = painterResource(id = R.drawable.play_circle_fill),
            contentDescription = null,
            modifier = Modifier
              .size(48.dp)
              .align(Alignment.Center)
          )
        }
      }
      StatusMediaType.UNKNOWN -> {
        val context = LocalContext.current
        Card(
          modifier = modifier.fillMaxWidth(),
          onClick = {
            launchCustomChromeTab(
              context = context,
              uri = Uri.parse(media.remoteUrl)
            )
          },
          colors = CardDefaults.elevatedCardColors(
            containerColor = AppTheme.colors.cardBackground,
            contentColor = AppTheme.colors.primaryContent,
            disabledContainerColor = AppTheme.colors.cardBackground,
            disabledContentColor = AppTheme.colors.primaryContent,
          ),
          enabled = media.remoteUrl != null
        ) {
          CenterRow(Modifier.padding(12.dp)) {
            Box(Modifier.clip(CircleShape).background(AppTheme.colors.accent, CircleShape)) {
              Icon(
                painter = painterResource(id = R.drawable.files),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.padding(12.dp).size(28.dp)
              )
            }
            WidthSpacer(value = 8.dp)
            Column {
              Text(
                text = stringResource(id = R.string.unsupported_media_files),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
              )
              if (media.remoteUrl != null) {
                HeightSpacer(value = 2.dp)
                Text(
                  text = media.remoteUrl,
                  color = AppTheme.colors.primaryContent.copy(.65f),
                  fontWeight = FontWeight.Medium
                )
              }
            }
          }
        }
      }
    }
  }
}

enum class StatusMediaType {
  IMAGE, VIDEO, UNKNOWN;
  companion object {
    fun fromString(type: String): StatusMediaType {
      return when (type) {
        "image" -> IMAGE
        "video", "gifv", "audio" -> VIDEO
        else -> UNKNOWN
      }
    }
  }
}
