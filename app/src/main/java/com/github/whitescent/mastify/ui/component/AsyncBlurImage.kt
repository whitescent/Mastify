package com.github.whitescent.mastify.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toDrawable
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import com.github.whitescent.mastify.utils.BlurHashDecoder

private const val BLUR_BITMAP_SIZE = 16

@Composable
fun AsyncBlurImage(
  url: String?,
  blurHash: String,
  contentDescription: String?,
  modifier: Modifier = Modifier,
  transform: (AsyncImagePainter.State) -> AsyncImagePainter.State = AsyncImagePainter.DefaultTransform,
  onState: ((AsyncImagePainter.State) -> Unit)? = null,
  alignment: Alignment = Alignment.Center,
  contentScale: ContentScale = ContentScale.Fit,
  alpha: Float = DefaultAlpha,
  colorFilter: ColorFilter? = null,
  filterQuality: FilterQuality = DrawScope.DefaultFilterQuality,
) {
  val resources = LocalContext.current.resources
  val blurPlaceholder = remember(blurHash) {
    BlurHashDecoder.decode(
      blurHash,
      BLUR_BITMAP_SIZE,
      BLUR_BITMAP_SIZE
    )?.toDrawable(resources)
  }
  AsyncImage(
    ImageRequest.Builder(LocalContext.current)
      .data(url)
      .placeholder(
        blurPlaceholder
      )
      .build(),
    contentDescription,
    modifier,
    transform,
    onState,
    alignment,
    contentScale,
    alpha,
    colorFilter,
    filterQuality
  )
}
