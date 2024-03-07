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
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.github.whitescent.mastify.ui.component.CenterRow
import com.github.whitescent.mastify.ui.component.WidthSpacer
import com.github.whitescent.mastify.ui.theme.AppTheme

@Composable
fun TextProgressBar(
  textLength: Int,
  maxTextLength: Int,
  modifier: Modifier = Modifier,
  fontSize: TextUnit = TextUnit.Unspecified
) {
  val progress = textLength / maxTextLength.toFloat()
  val colorAnimation by animateColorAsState(
    targetValue = when (progress) {
      in 0f..0.75f -> AppTheme.colors.accent
      in 0.75f..1f -> Color(0xFFE56305)
      else -> Color(0xFFF53232)
    }
  )
  CenterRow(Modifier.animateContentSize()) {
    CircularProgressIndicator(
      progress = { progress },
      modifier = modifier.size(24.dp),
      color = colorAnimation,
      strokeCap = StrokeCap.Round,
      trackColor = Color(0xFFD9D9D9)
    )
    WidthSpacer(value = 4.dp)
    Text(
      text = buildAnnotatedString {
        pushStyle(
          SpanStyle(
            color = if (textLength <= maxTextLength)
              AppTheme.colors.primaryContent.copy(alpha = 0.48f)
            else Color(0xFFF53232),
            fontSize = fontSize
          )
        )
        append("$textLength/$maxTextLength")
        pop()
      }
    )
  }
}
