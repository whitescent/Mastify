package com.github.whitescent.mastify.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

@Composable
fun CircleShapeImage(
  painter: Painter,
  modifier: Modifier = Modifier,
  border: BorderStroke? = null,
  contentScale: ContentScale = ContentScale.Fit
) {
  Surface(
    modifier = modifier,
    shape = CircleShape,
    border = border
  ) {
    Image(
      painter = painter,
      contentDescription = null,
      contentScale = contentScale
    )
  }
}

@Composable
fun CircleShapeAsyncImage(
  model: Any?,
  modifier: Modifier = Modifier,
  border: BorderStroke? = null,
  contentScale: ContentScale = ContentScale.Fit,
  alpha: Float = 1f,
  colorFilter: ColorFilter? = null
) {
  Surface(
    modifier = modifier,
    shape = CircleShape,
    border = border
  ) {
    AsyncImage(
      model = model,
      contentDescription = null,
      contentScale = contentScale,
      alpha = alpha,
      colorFilter = colorFilter
    )
  }
}
