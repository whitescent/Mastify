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

package com.github.whitescent.mastify.screen.post

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.whitescent.R
import com.github.whitescent.mastify.data.model.ui.StatusUiData
import com.github.whitescent.mastify.data.model.ui.StatusUiData.Visibility.Direct
import com.github.whitescent.mastify.data.model.ui.StatusUiData.Visibility.Private
import com.github.whitescent.mastify.data.model.ui.StatusUiData.Visibility.Public
import com.github.whitescent.mastify.data.model.ui.StatusUiData.Visibility.Unlisted
import com.github.whitescent.mastify.ui.component.CenterRow
import com.github.whitescent.mastify.ui.component.WidthSpacer
import com.github.whitescent.mastify.ui.theme.AppTheme

@Composable
fun PostVisibilityButton(
  visibility: StatusUiData.Visibility,
  openSheet: () -> Unit,
) {
  Surface(
    color = AppTheme.colors.background,
    shape = AppTheme.shape.mediumAvatar,
    border = BorderStroke(1.dp, Color(0xFF777777)),
    onClick = openSheet
  ) {
    CenterRow(Modifier.padding(vertical = 6.dp, horizontal = 8.dp)) {
      Icon(
        painter = when (visibility) {
          Public -> painterResource(R.drawable.globe)
          Unlisted -> painterResource(R.drawable.lock_open)
          Private -> painterResource(R.drawable.lock)
          Direct -> painterResource(R.drawable.at)
          else -> throw IllegalArgumentException("Invalid visibility")
        },
        contentDescription = null,
        modifier = Modifier.size(20.dp),
        tint = Color(0xFF777777)
      )
      WidthSpacer(value = 4.dp)
      Text(
        text = stringResource(
          id = when (visibility) {
            Public -> R.string.public_title
            Unlisted -> R.string.unlisted
            Private -> R.string.private_title
            Direct -> R.string.direct
            else -> throw IllegalArgumentException("Invalid visibility")
          },
        ),
        color = Color(0xFF777777)
      )
    }
  }
}
