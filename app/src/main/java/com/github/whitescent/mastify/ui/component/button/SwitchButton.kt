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

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.github.whitescent.mastify.ui.theme.AppTheme
import com.github.whitescent.mastify.utils.clickableWithoutIndication

@Composable
fun SwitchButton(
  modifier: Modifier = Modifier,
  enabled: Boolean,
  onValueChange: (Boolean) -> Unit
) {
  val density = LocalDensity.current
  val backgroundColor by animateColorAsState(
    targetValue = if (enabled) AppTheme.colors.accent else Color(0xFFe9e8eb)
  )
  var boxSize by remember { mutableStateOf(IntSize(0, 0)) }
  val switchBoxSize by remember(boxSize) {
    with(density) {
      mutableStateOf(boxSize.height.toDp() - 8.dp)
    }
  }
  Box(
    modifier = modifier
      .defaultMinSize(
        minWidth = 50.dp,
        minHeight = 28.dp
      )
      .onSizeChanged {
        boxSize = it
      }
      .clip(CircleShape)
      .background(backgroundColor, CircleShape)
      .clickableWithoutIndication { onValueChange(!enabled) }
  ) {
    val offsetAnimation by animateIntAsState(
      targetValue = when (enabled) {
        true -> with(density) {
          boxSize.width - switchBoxSize.roundToPx() - (switchBoxHorizontalPadding * 2).roundToPx()
        }
        else -> 0
      }
    )
    Box(
      modifier = modifier
        .padding(horizontal = switchBoxHorizontalPadding)
        .offset { IntOffset(offsetAnimation, 0) }
        .clip(CircleShape)
        .background(Color.White, CircleShape)
        .size(switchBoxSize)
        .align(Alignment.CenterStart)
    )
  }
}

private val switchBoxHorizontalPadding = 4.dp
