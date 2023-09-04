package com.github.whitescent.mastify.ui.component.status

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.whitescent.R
import com.github.whitescent.mastify.ui.component.CenterRow
import com.github.whitescent.mastify.ui.component.WidthSpacer
import kotlinx.coroutines.delay

@Composable
fun StatusSnackBar(
  show: Boolean,
  modifier: Modifier = Modifier,
  snackBarType: StatusSnackBarType = StatusSnackBarType.TEXT,
  onSnackBarClosed: () -> Unit = { }
) {
  var visible by remember(show) { mutableStateOf(show) }
  AnimatedVisibility(
    visible = visible,
    enter = fadeIn(),
    exit = fadeOut(),
    modifier = modifier.fillMaxWidth()
  ) {
    Surface(
      shape = RoundedCornerShape(12.dp),
      color = when (snackBarType) {
        StatusSnackBarType.TEXT -> Color(0xFF35465E)
        StatusSnackBarType.LINK -> Color(0xFF1B7CFF)
        StatusSnackBarType.BOOKMARK -> Color(0xFF498AE0)
      },
      contentColor = Color.White,
      shadowElevation = 4.dp
    ) {
      CenterRow(Modifier.padding(horizontal = 22.dp, vertical = 16.dp)) {
        Icon(
          painter = painterResource(
            id = when (snackBarType) {
              StatusSnackBarType.TEXT -> R.drawable.copy_fill
              StatusSnackBarType.LINK -> R.drawable.link_simple
              StatusSnackBarType.BOOKMARK -> R.drawable.bookmark_fill
            }
          ),
          contentDescription = null,
          modifier = Modifier.size(24.dp)
        )
        WidthSpacer(value = 8.dp)
        Text(
          text = stringResource(
            when (snackBarType) {
              StatusSnackBarType.TEXT -> R.string.text_copied
              StatusSnackBarType.LINK -> R.string.link_copied
              StatusSnackBarType.BOOKMARK -> R.string.bookmarked_snackBar
            }
          ),
          fontSize = 16.sp,
        )
      }
    }
  }
  LaunchedEffect(show) {
    if (show) {
      delay(2500)
      visible = false
      onSnackBarClosed()
    }
  }
}

enum class StatusSnackBarType {
  TEXT, LINK, BOOKMARK
}
