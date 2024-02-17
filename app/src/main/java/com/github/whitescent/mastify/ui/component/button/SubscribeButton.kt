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

package com.github.whitescent.mastify.ui.component.button

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.whitescent.R
import com.github.whitescent.mastify.ui.component.CenterRow
import com.github.whitescent.mastify.ui.component.WidthSpacer
import com.github.whitescent.mastify.ui.theme.AppTheme
import com.github.whitescent.mastify.utils.PostState

@Composable
fun SubscribeButton(
  subscribed: Boolean,
  modifier: Modifier = Modifier,
  postState: PostState,
  shape: Shape = AppTheme.shape.smallAvatar,
  onClick: (Boolean) -> Unit
) {
  Box(
    modifier = modifier
      .defaultMinSize(
        minWidth = ButtonDefaults.MinWidth,
        minHeight = ButtonDefaults.MinHeight
      )
      .background(
        color = when (subscribed) {
          true -> Color(0xFF000000).copy(.81f)
          else -> Color(0xFF5E12FF)
        },
        shape = shape
      )
      .clip(shape)
      .clickable {
        onClick(!subscribed)
      },
    contentAlignment = Alignment.Center
  ) {
    CenterRow(Modifier.padding(10.dp)) {
      when (postState == PostState.Posting) {
        true -> CircularProgressIndicator(
          color = Color.White,
          modifier = Modifier.size(24.dp),
          strokeWidth = 1.5.dp
        )
        else -> {
          Icon(
            painter = painterResource(
              id = when (subscribed) {
                true -> R.drawable.unsubscribe_bell
                else -> R.drawable.subscribe_bell
              }
            ),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(20.dp),
          )
          WidthSpacer(value = 8.dp)
          Text(
            text = stringResource(
              id = when (subscribed) {
                true -> R.string.unsubscribe_title
                else -> R.string.subscribe_title
              }
            ),
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
          )
        }
      }
    }
  }
}
