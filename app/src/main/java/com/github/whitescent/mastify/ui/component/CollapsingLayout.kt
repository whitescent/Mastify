package com.github.whitescent.mastify.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun CollapsingLayout(
  collapsingTop: @Composable BoxScope.() -> Unit,
  bodyContent: @Composable BoxScope.() -> Unit,
  topBar: @Composable (Float) -> Unit,
  modifier: Modifier = Modifier,
) {
  var collapsingTopHeight by remember { mutableFloatStateOf(0f) }
  var topBarHeight by remember { mutableFloatStateOf(0f) }
  var topBarAlpha by remember { mutableFloatStateOf(0f) }

  var offset by remember { mutableFloatStateOf(0f) }

  fun calculateOffset(delta: Float): Offset {
    val oldOffset = offset
    val newOffset = (oldOffset + delta).coerceIn(-(collapsingTopHeight-topBarHeight), 0f)
    offset = newOffset
    topBarAlpha = abs(offset / (collapsingTopHeight - topBarHeight))
    return Offset(0f, newOffset - oldOffset)
  }

  val nestedScrollConnection = remember {
    object : NestedScrollConnection {
      override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        return when {
          available.y >= 0 -> Offset.Zero
          offset == -(collapsingTopHeight - topBarHeight) -> Offset.Zero
          else -> calculateOffset(available.y)
        }
      }

      override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource,
      ): Offset = calculateOffset(available.y)

      override suspend fun onPreFling(available: Velocity): Velocity {
        return super.onPreFling(available)
      }

      override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
        return super.onPostFling(consumed, available)
      }
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .nestedScroll(nestedScrollConnection),
  ) {
    Box(
      modifier = Modifier
        .onSizeChanged { size ->
          collapsingTopHeight = size.height.toFloat()
        }
        .offset { IntOffset(x = 0, y = offset.roundToInt()) },
      content = collapsingTop,
    )
    Box(
      modifier = Modifier
        .padding(top = 16.dp)
        .offset {
          IntOffset(
            x = 0,
            y = (collapsingTopHeight + offset).roundToInt()
          )
        },
      content = bodyContent,
    )
    Box(
      modifier = Modifier
        .onSizeChanged {
          topBarHeight = it.height.toFloat()
        },
    ) {
      topBar(topBarAlpha)
    }
  }
}
