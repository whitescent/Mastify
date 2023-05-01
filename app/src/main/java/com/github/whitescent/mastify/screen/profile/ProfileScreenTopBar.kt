package com.github.whitescent.mastify.screen.profile

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.github.whitescent.mastify.AppTheme
import com.github.whitescent.mastify.data.model.AccountModel
import com.github.whitescent.mastify.ui.component.CenterRow
import com.github.whitescent.mastify.ui.component.CircleShapeAsyncImage
import com.github.whitescent.mastify.ui.component.WidthSpacer

@Composable
fun ProfileScreenTopBar(
  header: String,
  account: AccountModel,
  alpha: Float
) {
  val alphaAnimatable = remember { Animatable(0f) }
  LaunchedEffect(alpha) {
    if (!alpha.isNaN()) {
      if (alpha == 1f) alphaAnimatable.animateTo(alpha, tween(400, easing = LinearEasing))
      else alphaAnimatable.snapTo(0f)
    }
  }
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .height(100.dp)
      .alpha(alphaAnimatable.value)
  ) {
    AsyncImage(
      model = header,
      contentDescription = null,
      contentScale = ContentScale.Crop,
      modifier = Modifier
        .fillMaxWidth()
        .blur(2.dp)
    )
    CenterRow(
      modifier = Modifier
        .padding(horizontal = 12.dp)
        .align(Alignment.CenterStart)
    ) {
      CircleShapeAsyncImage(
        model = account.avatar,
        modifier = Modifier.size(40.dp),
        contentScale = ContentScale.Crop
      )
      WidthSpacer(value = 12.dp)
      Text(
        text = account.username,
        style = AppTheme.typography.headlineSmall,
        color = Color.White
      )
    }
  }
}
