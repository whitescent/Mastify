package com.github.whitescent.mastify.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun ClickableIcon(
  imageVector: ImageVector,
  modifier: Modifier = Modifier,
  tint: Color = LocalContentColor.current
) {
  Icon(
    imageVector = imageVector,
    contentDescription = null,
    modifier = modifier.clickable(
      onClick = {

      },
      interactionSource = MutableInteractionSource(),
      indication = rememberRipple(
        bounded = false,
        radius = 24.dp,
        color = Color.Gray
      ),
    ),
    tint = tint
  )
}