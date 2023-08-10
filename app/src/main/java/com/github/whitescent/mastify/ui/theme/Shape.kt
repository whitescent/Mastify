package com.github.whitescent.mastify.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

class MastifyShape {
  val statusAvatarShape @Composable get() = RoundedCornerShape(10.dp)
}

val LocalMastifyShape = staticCompositionLocalOf {
  MastifyShape()
}
