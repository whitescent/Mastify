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

package com.github.whitescent.mastify.ui.component.status.action

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.github.whitescent.R
import com.github.whitescent.mastify.ui.component.ClickableIcon
import com.github.whitescent.mastify.ui.theme.AppTheme
import kotlinx.coroutines.launch

@Composable
fun ReblogButton(
  reblogged: Boolean,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  unreblogColor: Color = AppTheme.colors.cardAction,
  onClick: (Boolean) -> Unit,
) {
  val scope = rememberCoroutineScope()

  val scaleAnimatable = remember { Animatable(1f) }
  val rotateAnimatable = remember { Animatable(0f) }
  val animatedReblogIconColor by animateColorAsState(
    targetValue = if (reblogged) AppTheme.colors.reblogged else unreblogColor,
  )

  ClickableIcon(
    painter = painterResource(R.drawable.repeat),
    modifier = modifier.scale(scaleAnimatable.value).rotate(rotateAnimatable.value),
    tint = if (enabled) animatedReblogIconColor else unreblogColor.copy(0.34f),
    enabled = enabled
  ) {
    if (enabled) {
      onClick(!reblogged)
      scope.launch {
        rotateAnimatable.animateTo(
          targetValue = if (rotateAnimatable.value == 0f) 360f else 0f,
          animationSpec = tween(durationMillis = 300)
        )
        scaleAnimatable.animateTo(1.4f, animationSpec = tween(durationMillis = 150))
        scaleAnimatable.animateTo(1f, animationSpec = tween(durationMillis = 150))
      }
    }
  }
}
