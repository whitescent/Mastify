package com.github.whitescent.mastify.ui.component.nestedscrollview

import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import kotlin.math.roundToInt

/**
 * Define a [VerticalNestedScrollView].
 *
 * @param state the state object to be used to observe the [VerticalNestedScrollView] state.
 * @param modifier the modifier to apply to this layout.
 * @param content a block which describes the header.
 * @param content a block which describes the content.
 */
@Composable
fun VerticalNestedScrollView(
  modifier: Modifier = Modifier,
  state: NestedScrollViewState,
  header: @Composable () -> Unit = {},
  content: @Composable () -> Unit = {},
) {
  NestedScrollView(
    modifier = modifier,
    state = state,
    orientation = Orientation.Vertical,
    header = header,
    content = content,
  )
}

/**
 * Define a [HorizontalNestedScrollView].
 *
 * @param state the state object to be used to observe the [HorizontalNestedScrollView] state.
 * @param modifier the modifier to apply to this layout.
 * @param content a block which describes the header.
 * @param content a block which describes the content.
 */
@Composable
fun HorizontalNestedScrollView(
  modifier: Modifier = Modifier,
  state: NestedScrollViewState,
  header: @Composable () -> Unit = {},
  content: @Composable () -> Unit = {},
) {
  NestedScrollView(
    modifier = modifier,
    state = state,
    orientation = Orientation.Horizontal,
    header = header,
    content = content,
  )
}

@Composable
private fun NestedScrollView(
  modifier: Modifier = Modifier,
  state: NestedScrollViewState,
  orientation: Orientation,
  header: @Composable () -> Unit,
  content: @Composable () -> Unit,
) {
  Layout(
    modifier = modifier
      .scrollable(
        orientation = orientation,
        state = rememberScrollableState {
          state.drag(it)
        }
      )
      .nestedScroll(state.nestedScrollConnectionHolder),
    content = {
      Box {
        header.invoke()
      }
      Box {
        content.invoke()
      }
    },
  ) { measurables, constraints ->
    layout(constraints.maxWidth, constraints.maxHeight) {
      when (orientation) {
        Orientation.Vertical -> {
          val headerPlaceable =
            measurables[0].measure(constraints.copy(maxHeight = Constraints.Infinity))
          headerPlaceable.place(0, state.offset.roundToInt())
          state.updateBounds(-(headerPlaceable.height.toFloat()))
          val contentPlaceable =
            measurables[1].measure(constraints.copy(maxHeight = constraints.maxHeight))
          contentPlaceable.place(
            0,
            state.offset.roundToInt() + headerPlaceable.height
          )
        }

        Orientation.Horizontal -> {
          val headerPlaceable =
            measurables[0].measure(constraints.copy(maxWidth = Constraints.Infinity))
          headerPlaceable.place(state.offset.roundToInt(), 0)
          state.updateBounds(-(headerPlaceable.width.toFloat()))
          val contentPlaceable =
            measurables[1].measure(constraints.copy(maxWidth = constraints.maxWidth))
          contentPlaceable.place(
            state.offset.roundToInt() + headerPlaceable.width,
            0,
          )
        }
      }
    }
  }
}
