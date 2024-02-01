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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.github.whitescent.R
import com.github.whitescent.mastify.network.model.status.Card
import com.github.whitescent.mastify.ui.component.CenterRow
import com.github.whitescent.mastify.ui.component.HeightSpacer
import com.github.whitescent.mastify.ui.component.WidthSpacer
import com.github.whitescent.mastify.ui.theme.AppTheme
import com.github.whitescent.mastify.utils.launchCustomChromeTab

@Composable
fun StatusLinkPreviewCard(
  card: Card?,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val toolbarColor = AppTheme.colors.primaryContent
  if (card != null) {
    Card(
      modifier = modifier.fillMaxWidth().height(IntrinsicSize.Min),
      onClick = {
        launchCustomChromeTab(
          context = context,
          uri = Uri.parse(card.url),
          toolbarColor = toolbarColor.toArgb(),
        )
      },
      shape = AppTheme.shape.mediumAvatar,
      colors = CardDefaults.elevatedCardColors(
        containerColor = AppTheme.colors.cardBackground,
        contentColor = AppTheme.colors.primaryContent
      ),
      border = BorderStroke(1.dp, AppTheme.colors.divider)
    ) {
      CenterRow(Modifier.fillMaxWidth()) {
        if (!card.image.isNullOrEmpty()) {
          AsyncImage(
            model = card.image,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxHeight().width(100.dp)
          )
          WidthSpacer(value = 6.dp)
        }
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 12.dp),
          verticalArrangement = Arrangement.Center
        ) {
          when {
            card.title.isNotBlank() && card.description.isNotBlank() -> {
              Text(
                text = card.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
              )
              HeightSpacer(value = 4.dp)
              Text(
                text = card.description,
                fontSize = 12.sp,
                color = AppTheme.colors.primaryContent.copy(0.6f),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
              )
            }
            card.title.isNotBlank() -> Text(
              text = card.title,
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              maxLines = 2,
              overflow = TextOverflow.Ellipsis,
            )
            card.description.isNotBlank() -> Text(
              text = card.description,
              fontSize = 12.sp,
              color = AppTheme.colors.primaryContent.copy(0.6f),
              maxLines = 3,
              overflow = TextOverflow.Ellipsis
            )
          }
          if (card.image.isNullOrEmpty()) {
            HeightSpacer(value = 4.dp)
            CenterRow(Modifier.align(Alignment.End)) {
              Icon(
                painter = painterResource(id = R.drawable.link_simple),
                contentDescription = null,
                tint = AppTheme.colors.accent.copy(0.8f),
                modifier = Modifier.size(20.dp)
              )
              WidthSpacer(value = 2.dp)
              Text(
                text = stringResource(id = R.string.link_preview_title),
                fontSize = 12.sp,
                color = AppTheme.colors.primaryContent.copy(0.6f),
              )
            }
          }
        }
      }
    }
  }
}
