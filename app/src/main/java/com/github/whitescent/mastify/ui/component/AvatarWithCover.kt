package com.github.whitescent.mastify.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Composable
fun AvatarWithCover(
  cover: @Composable () -> Unit,
  avatar: @Composable () -> Unit
) {
  val startPadding = with(LocalDensity.current) { avatarStartPadding.roundToPx() }
  Layout(
    content = {
      cover()
      avatar()
    }
  ) { measurables, constraints ->
    val placeables = measurables.map { measurable ->
      measurable.measure(constraints)
    }
    val coverPlaceable = placeables[0]
    val avatarPlaceable = placeables[1]
    layout(coverPlaceable.width, coverPlaceable.height + avatarPlaceable.height / 2) {
      coverPlaceable.place(0, 0)
      avatarPlaceable.place(startPadding, coverPlaceable.height - avatarPlaceable.height / 2)
    }
  }
}

val avatarStartPadding = 20.dp
