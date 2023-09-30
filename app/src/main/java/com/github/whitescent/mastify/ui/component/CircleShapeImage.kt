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

package com.github.whitescent.mastify.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.github.whitescent.mastify.ui.theme.AppTheme

@Composable
fun CircleShapeAsyncImage(
  model: Any?,
  modifier: Modifier = Modifier,
  border: BorderStroke? = null,
  shape: Shape = AppTheme.shape.smallAvatar,
  contentScale: ContentScale = ContentScale.Fit,
  alpha: Float = 1f,
  colorFilter: ColorFilter? = null,
  onClick: (() -> Unit)? = null
) {
  Surface(
    modifier = modifier,
    shape = shape,
    border = border
  ) {
    AsyncImage(
      model = model,
      contentDescription = null,
      contentScale = contentScale,
      alpha = alpha,
      colorFilter = colorFilter,
      modifier = Modifier.clickable(
        enabled = onClick != null,
        onClick = { onClick?.invoke() }
      )
    )
  }
}
