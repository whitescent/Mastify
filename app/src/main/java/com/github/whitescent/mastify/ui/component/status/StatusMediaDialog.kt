package com.github.whitescent.mastify.ui.component.status

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.github.whitescent.mastify.network.model.response.account.MediaAttachments
import com.github.whitescent.mastify.utils.LocalSystemUiController
import com.mxalbert.zoomable.Zoomable

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StatusMediaDialog(
  avatar: String,
  content: String,
  media: List<MediaAttachments>,
  targetMediaIndex: Int,
  onDismissRequest: () -> Unit
) {
  val systemUiController = LocalSystemUiController.current
  val useDarkIcons = !isSystemInDarkTheme()
  Dialog(
    onDismissRequest = onDismissRequest,
    properties = DialogProperties(
      usePlatformDefaultWidth = false
    )
  ) {
    val pagerState = rememberPagerState(initialPage = targetMediaIndex)
    HorizontalPager(
      state = pagerState,
      pageCount = media.size,
      modifier = Modifier
        .fillMaxSize()
        .background(Color.Black)
    ) { page ->
      Zoomable {
        val painter = rememberAsyncImagePainter(
          model = ImageRequest.Builder(LocalContext.current)
            .data(media[page].url)
            .size(Size.ORIGINAL)
            .build()
        )
        if (painter.state is AsyncImagePainter.State.Success) {
          val size = painter.intrinsicSize
          Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier
              .aspectRatio(size.width / size.height)
              .fillMaxSize()
          )
        }
      }
    }
  }
  DisposableEffect(Unit) {
    systemUiController.setSystemBarsColor(
      color = Color.Black,
      darkIcons = false
    )
    onDispose {
      systemUiController.setSystemBarsColor(
        color = Color.Transparent,
        darkIcons = useDarkIcons
      )
    }
  }
}
