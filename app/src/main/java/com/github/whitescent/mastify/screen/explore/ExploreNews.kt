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

package com.github.whitescent.mastify.screen.explore

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.whitescent.mastify.network.model.trends.News
import com.github.whitescent.mastify.ui.component.AsyncBlurImage
import com.github.whitescent.mastify.ui.component.CenterRow
import com.github.whitescent.mastify.ui.component.HeightSpacer
import com.github.whitescent.mastify.ui.component.WidthSpacer
import com.github.whitescent.mastify.ui.theme.AppTheme
import com.github.whitescent.mastify.utils.FormatFactory.getRelativeTimeSpanString
import com.github.whitescent.mastify.utils.launchCustomChromeTab
import kotlinx.datetime.toInstant

@Composable
fun ExploreNewsItem(
  news: News
) {
  val context = LocalContext.current
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(AppTheme.shape.mediumAvatar)
      .height(180.dp)
      .clickable {
        launchCustomChromeTab(
          context = context,
          uri = Uri.parse(news.url)
        )
      }
  ) {
    AsyncBlurImage(
      url = news.image,
      blurHash = news.blurhash,
      contentDescription = null,
      modifier = Modifier.fillMaxSize(),
      contentScale = ContentScale.Crop
    )
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)))
    Column(
      modifier = Modifier
        .align(Alignment.BottomStart)
        .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
      Text(
        text = news.title.ifEmpty { news.description },
        color = Color.White,
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold,
        overflow = Ellipsis,
        maxLines = 2
      )
      HeightSpacer(value = 4.dp)
      CenterRow {
        Text(
          text = news.providername,
          color = Color.White,
          fontSize = 14.sp
        )
        news.publishedAt?.let {
          WidthSpacer(value = 4.dp)
          Box(Modifier.size(2.dp).background(Color.White, CircleShape))
          WidthSpacer(value = 4.dp)
          Text(
            text = getRelativeTimeSpanString(news.publishedAt.toInstant().toEpochMilliseconds()),
            color = Color.White,
            fontSize = 14.sp
          )
        }
      }
    }
  }
}
