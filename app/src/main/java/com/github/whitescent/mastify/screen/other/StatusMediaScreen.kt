package com.github.whitescent.mastify.screen.other

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.github.whitescent.mastify.AppNavGraph
import com.github.whitescent.mastify.network.model.status.Status.Attachment
import com.github.whitescent.mastify.ui.transitions.StatusMediaTransitions
import com.mxalbert.zoomable.Zoomable
import com.ramcosta.composedestinations.annotation.Destination

@OptIn(ExperimentalFoundationApi::class)
@AppNavGraph
@Destination(style = StatusMediaTransitions::class)
@Composable
fun StatusMediaScreen(
  attachments: Array<Attachment>,
  targetMediaIndex: Int,
) {
  var hideInfo by remember { mutableStateOf(false) }
  val pagerState = rememberPagerState(
    initialPage = targetMediaIndex,
    pageCount = { attachments.size }
  )
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color.Black)
  ) {
    HorizontalPager(
      state = pagerState,
      pageContent = {
        Zoomable(
          modifier = Modifier.fillMaxSize(),
          onTap = {
            hideInfo = !hideInfo
          }
        ) {
          val painter = rememberAsyncImagePainter(
            model = ImageRequest.Builder(LocalContext.current)
              .data(attachments[it].url)
              .size(Size.ORIGINAL)
              .transformations()
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
    )
  }
}
