package com.github.whitescent.mastify.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp

@Composable
fun ClickableIcon(
  painter: Painter,
  modifier: Modifier = Modifier,
  tint: Color = LocalContentColor.current,
  onClick: (() -> Unit)? = null
) {
  Icon(
    painter = painter,
    contentDescription = null,
    modifier = modifier.clickable(
      onClick = {
        onClick?.invoke()
      },
      interactionSource = remember { MutableInteractionSource() },
      indication = rememberRipple(
        bounded = false,
        radius = 20.dp,
        color = Color.Gray
      ),
    ),
    tint = tint
  )
}
