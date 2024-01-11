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

import androidx.compose.runtime.Composable
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Composable
fun AvatarWithCover(
  cover: @Composable () -> Unit,
  avatar: @Composable () -> Unit,
  actions: @Composable (() -> Unit)? = null,
) {
  val startPadding = with(LocalDensity.current) { avatarStartPadding.roundToPx() }
  Layout(
    content = {
      cover()
      avatar()
      actions?.invoke()
    }
  ) { measurables, constraints ->
    val placeables = measurables.map { measurable ->
      measurable.measure(constraints)
    }
    val coverPlaceable = placeables[0]
    val avatarPlaceable = placeables[1]
    val actionsPlaceable = placeables.getOrNull(2)
    layout(coverPlaceable.width, coverPlaceable.height + avatarPlaceable.height / 2) {
      coverPlaceable.place(0, 0)
      avatarPlaceable.place(startPadding, coverPlaceable.height - avatarPlaceable.height / 2)
      actionsPlaceable?.place(
        x = coverPlaceable.width - actionsPlaceable.width - startPadding,
        y = coverPlaceable.height
      )
    }
  }
}

val avatarStartPadding = 20.dp
