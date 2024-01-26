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

import androidx.compose.foundation.Indication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LocalRippleConfiguration
import androidx.compose.material.ripple
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ClickableIcon(
  painter: Painter,
  modifier: Modifier = Modifier,
  interactiveSize: Dp = 24.dp,
  interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
  indication: Indication = ripple(
    bounded = false,
    radius = interactiveSize / 1.4f,
    color = LocalRippleConfiguration.current.color
  ),
  enabled: Boolean = true,
  tint: Color = LocalContentColor.current,
  onClick: (() -> Unit)? = null
) {
  Icon(
    painter = painter,
    contentDescription = null,
    modifier = modifier
      .size(interactiveSize)
      .clickable(
        role = Role.Button,
        onClick = {
          onClick?.invoke()
        },
        interactionSource = interactionSource,
        indication = indication,
        enabled = enabled
      ),
    tint = if (enabled) tint else tint.copy(alpha = 0.5f)
  )
}
