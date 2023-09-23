/*
 * Copyright 2023 WhiteScent
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

package com.github.whitescent.mastify.screen.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.github.whitescent.R
import com.github.whitescent.mastify.ui.component.CenterRow
import com.github.whitescent.mastify.ui.component.CircleShapeAsyncImage
import com.github.whitescent.mastify.ui.component.WidthSpacer
import com.github.whitescent.mastify.ui.theme.AppTheme

@Composable
fun HomeTopBar(
  avatar: String,
  modifier: Modifier = Modifier,
  openDrawer: () -> Unit
) {
  CenterRow(
    modifier = modifier
      .statusBarsPadding()
      .fillMaxWidth()
      .padding(24.dp)
  ) {
    Icon(
      painter = painterResource(id = R.drawable.logo_text),
      contentDescription = null,
      tint = AppTheme.colors.primaryContent,
    )
    Spacer(modifier = Modifier.weight(1f))
    CenterRow {
      Image(
        painter = painterResource(id = R.drawable.filter),
        contentDescription = null,
        modifier = Modifier.size(24.dp)
      )
      WidthSpacer(value = 8.dp)
      CircleShapeAsyncImage(
        model = avatar,
        modifier = Modifier
          .size(42.dp),
        onClick = {
          openDrawer()
        },
        shape = AppTheme.shape.avatarShape
      )
    }
  }
}
