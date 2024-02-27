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

package com.github.whitescent.mastify.screen.profile

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.github.whitescent.R
import com.github.whitescent.mastify.network.model.account.Account
import com.github.whitescent.mastify.ui.component.AnimatedVisibility
import com.github.whitescent.mastify.ui.component.CenterRow
import com.github.whitescent.mastify.ui.component.CircleShapeAsyncImage
import com.github.whitescent.mastify.ui.component.TextWithEmoji
import com.github.whitescent.mastify.ui.component.WidthSpacer
import com.github.whitescent.mastify.ui.theme.AppTheme

@Composable
fun ProfileTopBar(
  alpha: () -> Float,
  account: Account,
) {
  val defaultBackgroundColor = AppTheme.colors.defaultHeader
  val density = LocalDensity.current
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .height(WindowInsets.statusBars.getTop(density).dp)
  ) {
    AsyncImage(
      model = ImageRequest.Builder(LocalContext.current)
        .data(account.header)
        .build(),
      contentDescription = null,
      modifier = Modifier
        .fillMaxSize()
        .alpha(alpha())
        .drawWithContent {
          drawRect(defaultBackgroundColor)
          drawContent()
          drawRect(Color.Black.copy(0.35f))
        },
      contentScale = ContentScale.Crop
    )
    AnimatedVisibility(
      visible = alpha() >= 1,
      enter = slideInVertically { it } + fadeIn(),
      exit = slideOutVertically { it / 2 } + fadeOut(),
      modifier = Modifier.fillMaxSize()
    ) {
      CenterRow(Modifier.statusBarsPadding().padding(start = 24.dp).width(280.dp)) {
        CircleShapeAsyncImage(
          model = account.avatar,
          modifier = Modifier.size(36.dp),
          shape = AppTheme.shape.smallAvatar
        )
        WidthSpacer(value = 8.dp)
        Column {
          TextWithEmoji(
            text = account.realDisplayName,
            emojis = account.emojis,
            fontSize = 18.sp,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Text(
            text = pluralStringResource(
              R.plurals.post_count,
              account.statusesCount.toInt(),
              account.statusesCount,
            ),
            fontSize = 14.sp,
            color = Color.White,
          )
        }
      }
    }
  }
}
