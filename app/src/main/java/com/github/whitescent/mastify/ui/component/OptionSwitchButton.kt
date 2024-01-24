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

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.whitescent.mastify.ui.theme.AppTheme

data class PollSwitchOption(
  val text: String,
  val icon: Painter? = null,
  val isSelected: Boolean = false
)

@Composable
fun OptionSwitchButton(
  options: List<PollSwitchOption>,
  onClick: (Int) -> Unit
) {
  var selected by rememberSaveable(options) { mutableIntStateOf(0) }
  var maximumItemHeight by remember { mutableStateOf(0.dp) }
  val optionsMeasuredWidth = remember { mutableStateListOf<Int>() }
  val optionsPositions = remember { mutableStateListOf<Int>() }

  val animatedWidth by animateDpAsState(
    targetValue = with(LocalDensity.current) {
      if (optionsMeasuredWidth.isNotEmpty()) optionsMeasuredWidth[selected].toDp() else 0.dp
    },
    animationSpec = spring(stiffness = Spring.StiffnessLow)
  )

  val animatedOffsetX by animateDpAsState(
    targetValue = with(LocalDensity.current) {
      if (optionsPositions.isNotEmpty()) optionsPositions[selected].toDp() else 0.dp
    },
    animationSpec = spring(stiffness = Spring.StiffnessLow)
  )

  Box(
    modifier = Modifier
      .border(0.5.dp, AppTheme.colors.replyLine, AppTheme.shape.betweenSmallAndMediumAvatar)
      .clip(AppTheme.shape.betweenSmallAndMediumAvatar)
  ) {
    if (optionsPositions.isNotEmpty()) {
      Box(
        modifier = Modifier
          .offset(x = animatedOffsetX)
          .clip(AppTheme.shape.betweenSmallAndMediumAvatar)
          .width(animatedWidth)
          .height(maximumItemHeight)
          .background(Color(0xFF046FFF))
      )
    }
    Layout(
      content = {
        options.forEachIndexed { index, item ->
          OptionSwitchButtonItem(
            text = item.text,
            selected = index == selected,
            icon = item.icon,
            onClick = {
              onClick(index)
              selected = index
            }
          )
        }
      }
    ) { measurables, constraints ->
      val placeables = measurables.map { measurable ->
        measurable.measure(constraints)
      }
      if (optionsMeasuredWidth.isEmpty()) {
        var accumulatedOffset = 0
        optionsPositions.addAll(
          placeables.map { placeable ->
            val position = accumulatedOffset
            accumulatedOffset += placeable.width
            position
          }
        )
        optionsMeasuredWidth.addAll(placeables.map { it.width })
      }
      val width = placeables.sumOf { it.width }
      val height = placeables.maxOf { it.height }
      maximumItemHeight = height.toDp()
      layout(width, height) {
        var offsetX = 0
        placeables.forEach { placeable ->
          placeable.placeRelative(offsetX, 0)
          offsetX += placeable.width
        }
      }
    }
  }
}

@Composable
private fun OptionSwitchButtonItem(
  text: String,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  icon: Painter? = null,
) {
  Crossfade(selected) { isSelected ->
    CenterRow(
      modifier = modifier
        .clip(AppTheme.shape.betweenSmallAndMediumAvatar)
        .clickable { onClick() }
        .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
      if (icon != null) {
        Icon(
          painter = icon,
          contentDescription = null,
          modifier = Modifier.size(20.dp),
          tint = if (isSelected) Color.White else AppTheme.colors.cardAction
        )
        WidthSpacer(value = 4.dp)
      }
      Text(
        text = text,
        fontSize = 14.sp,
        color = if (isSelected) Color.White else AppTheme.colors.cardAction
      )
    }
  }
}
