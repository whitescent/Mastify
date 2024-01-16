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

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.github.whitescent.mastify.ui.component.CenterRow
import com.github.whitescent.mastify.ui.component.WidthSpacer
import com.github.whitescent.mastify.ui.theme.AppTheme

@Composable
fun TextProgressBar(
  textProgress: Float,
  progress: @Composable () -> Unit
) {
  val colorAnimation by animateColorAsState(
    targetValue = when (textProgress) {
      in 0f..0.8f -> AppTheme.colors.accent
      in 0.8f..1f -> Color(0xFFE56305)
      else -> Color(0xFFF53232)
    }
  )
  CenterRow(Modifier.animateContentSize()) {
    Box {
      Canvas(
        modifier = Modifier.size(24.dp)
      ) {
        drawCircle(
          color = Color(0xFFD9D9D9),
          radius = 10.dp.toPx(),
          style = Stroke(width = 2.dp.toPx())
        )
      }
      CircularProgressIndicator(
        progress = { textProgress },
        modifier = Modifier.size(24.dp),
        color = colorAnimation
      )
    }
    WidthSpacer(value = 4.dp)
    progress()
  }
}
