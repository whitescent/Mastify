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

package com.github.whitescent.mastify.ui.component.profileCollapsingLayout

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.Layout
import kotlin.math.roundToInt

@Composable
fun ProfileLayout(
  state: ProfileLayoutState,
  modifier: Modifier = Modifier,
  enabledScroll: Boolean = true,
  collapsingTop: @Composable () -> Unit,
  bodyContent: @Composable () -> Unit,
  topBar: @Composable () -> Unit,
) {
  Layout(
    modifier = modifier
      .scrollable(
        state = rememberScrollableState {
          state.calculateOffset(it)
        },
        orientation = Orientation.Vertical,
        enabled = enabledScroll
      )
      .nestedScroll(state.nestedScrollConnection),
    content = {
      collapsingTop()
      bodyContent()
      topBar()
    },
  ) { measurables, constraints ->
    val collapsingTopPlaceable = measurables[0].measure(constraints)
    val bodyContentPlaceable = measurables[1].measure(constraints)
    val topBarPlaceable = measurables[2].measure(constraints)
    state.bodyContentMaxHeight = (constraints.maxHeight - collapsingTopPlaceable.height).toDp()
    layout(constraints.maxWidth, constraints.maxHeight) {
      state.updateBounds((collapsingTopPlaceable.height - topBarPlaceable.height).toFloat())
      collapsingTopPlaceable.place(0, state.offset.value.roundToInt())
      bodyContentPlaceable.place(0, collapsingTopPlaceable.height + state.offset.value.roundToInt())
      topBarPlaceable.place(0, 0)
    }
  }
}
