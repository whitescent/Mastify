package com.github.whitescent.mastify.ui.component.status.action

import androidx.compose.animation.animateColorAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.github.whitescent.R
import com.github.whitescent.mastify.ui.component.ClickableIcon
import com.github.whitescent.mastify.ui.theme.AppTheme

@Composable
fun FavoriteButton(
  favorited: Boolean,
  modifier: Modifier = Modifier,
  unfavoritedColor: Color = AppTheme.colors.cardAction,
  onClick: (Boolean) -> Unit,
) {
  var favState by rememberSaveable(favorited) { mutableStateOf(favorited) }
  val animatedFavIconColor by animateColorAsState(
    targetValue = if (favState) AppTheme.colors.cardLike else unfavoritedColor,
  )

  ClickableIcon(
    painter = painterResource(id = if (favState) R.drawable.heart_fill else R.drawable.heart),
    modifier = modifier,
    tint = animatedFavIconColor,
  ) {
    favState = !favState
    onClick(favState)
  }
}
