package com.github.whitescent.mastify.ui.component.status.action

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.github.whitescent.R
import com.github.whitescent.mastify.ui.component.ClickableIcon
import com.github.whitescent.mastify.ui.theme.AppTheme
import kotlinx.coroutines.launch

@Composable
fun ReblogButton(
  reblogged: Boolean,
  modifier: Modifier = Modifier,
  unreblogColor: Color = AppTheme.colors.cardAction,
  onClick: (Boolean) -> Unit,
) {
  val scope = rememberCoroutineScope()

  val reblogScaleAnimatable = remember { Animatable(1f) }
  val reblogRotateAnimatable = remember { Animatable(0f) }
  var reblogState by remember(reblogged) { mutableStateOf(reblogged) }
  val animatedReblogIconColor by animateColorAsState(
    targetValue = if (reblogState) AppTheme.colors.reblogged else unreblogColor,
  )

  ClickableIcon(
    painter = painterResource(if (reblogState) R.drawable.share_fill else R.drawable.share_fat),
    modifier = modifier.scale(reblogScaleAnimatable.value).rotate(reblogRotateAnimatable.value),
    tint = animatedReblogIconColor,
  ) {
    reblogState = !reblogState
    onClick(reblogState)
    scope.launch {
      reblogRotateAnimatable.animateTo(
        targetValue = if (reblogRotateAnimatable.value == 0f) 360f else 0f,
        animationSpec = tween(durationMillis = 300)
      )
      reblogScaleAnimatable.animateTo(1.4f, animationSpec = tween(durationMillis = 150))
      reblogScaleAnimatable.animateTo(1f, animationSpec = tween(durationMillis = 150))
    }
  }
}
