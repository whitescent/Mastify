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
fun BookmarkButton(
  bookmarked: Boolean,
  modifier: Modifier = Modifier,
  unbookmarkedColor: Color = AppTheme.colors.primaryContent,
  onClick: (Boolean) -> Unit,
) {
  var bookmarkState by rememberSaveable(bookmarked) { mutableStateOf(bookmarked) }
  val animatedIconColor by animateColorAsState(
    targetValue = if (bookmarkState) Color(0xFF498AE0) else unbookmarkedColor,
  )
  ClickableIcon(
    painter = painterResource(
      id = when (bookmarkState) {
        true -> R.drawable.bookmark_fill
        else -> R.drawable.bookmark_simple
      }
    ),
    modifier = modifier,
    tint = animatedIconColor,
  ) {
    bookmarkState = !bookmarkState
    onClick(bookmarkState)
  }
}
