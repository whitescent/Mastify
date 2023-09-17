package com.github.whitescent.mastify.ui.component.profileCollapsingLayout

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
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
  val density = LocalDensity.current
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
    state.bodyContentMaxHeight = with(density) {
      (constraints.maxHeight - collapsingTopPlaceable.height).toDp()
    }
    layout(constraints.maxWidth, constraints.maxHeight) {
      state.updateBounds((collapsingTopPlaceable.height - topBarPlaceable.height).toFloat())
      collapsingTopPlaceable.place(0, state.offset.value.roundToInt())
      bodyContentPlaceable.place(0, collapsingTopPlaceable.height + state.offset.value.roundToInt())
      topBarPlaceable.place(0, 0)
    }
  }
}
