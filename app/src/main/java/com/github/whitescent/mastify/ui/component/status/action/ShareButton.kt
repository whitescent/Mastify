package com.github.whitescent.mastify.ui.component.status.action

import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.core.content.ContextCompat
import com.github.whitescent.R
import com.github.whitescent.mastify.ui.component.ClickableIcon
import com.github.whitescent.mastify.ui.theme.AppTheme

@Composable
fun ShareButton(
  link: String,
  modifier: Modifier = Modifier,
  @DrawableRes id: Int = R.drawable.share,
  tint: Color = AppTheme.colors.cardAction,
) {
  val context = LocalContext.current
  ClickableIcon(
    painter = painterResource(id = id),
    modifier = modifier,
    tint = tint,
  ) {
    val sendIntent: Intent = Intent(Intent.ACTION_SEND).apply {
      putExtra(Intent.EXTRA_TEXT, link)
      type = "text/html"
    }

    val shareIntent = Intent.createChooser(sendIntent, null)
    ContextCompat.startActivity(context, shareIntent, null)
  }
}
