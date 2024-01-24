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

package com.github.whitescent.mastify.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.whitescent.mastify.ui.theme.AppTheme

@Composable
fun AppHorizontalDivider(
  modifier: Modifier = Modifier,
  thickness: Dp = 0.5.dp,
  color: Color = AppTheme.colors.divider
) = HorizontalDivider(thickness = thickness, color = color, modifier = modifier)

@Composable
fun AppHorizontalDashedDivider(
  modifier: Modifier = Modifier,
  thickness: Dp = 2.dp,
  color: Color = AppTheme.colors.divider,
  phase: Float = 10f,
  intervals: FloatArray = floatArrayOf(10f, 10f),
) {
  Canvas(
    modifier = modifier
  ) {
    val dividerHeight = thickness.toPx()
    drawRoundRect(
      color = color,
      style = Stroke(
        width = dividerHeight,
        pathEffect = PathEffect.dashPathEffect(
          intervals = intervals,
          phase = phase
        )
      )
    )
  }
}
