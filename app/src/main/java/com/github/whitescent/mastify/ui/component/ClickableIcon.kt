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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp

@Composable
fun ClickableIcon(
  painter: Painter,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  tint: Color = LocalContentColor.current,
  onClick: (() -> Unit)? = null
) {
  Icon(
    painter = painter,
    contentDescription = null,
    modifier = modifier.clickable(
      onClick = {
        onClick?.invoke()
      },
      interactionSource = remember { MutableInteractionSource() },
      indication = rememberRipple(
        bounded = false,
        radius = 20.dp,
        color = Color.Gray
      ),
      enabled = enabled
    ),
    tint = if (enabled) tint else tint.copy(alpha = 0.5f)
  )
}
