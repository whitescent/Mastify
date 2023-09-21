package com.github.whitescent.mastify.ui.component.status.paging

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.github.whitescent.R
import com.github.whitescent.mastify.ui.theme.AppTheme

@Composable
fun EmptyStatusListPlaceholder(
  pageType: PageType,
  modifier: Modifier = Modifier,
  alignment: Alignment = Alignment.Center,
) {
  val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.empty_status))
  val progress by animateLottieCompositionAsState(composition, iterations = Int.MAX_VALUE)
  Box(
    modifier = modifier.fillMaxSize(),
    contentAlignment = alignment
  ) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
      LottieAnimation(
        composition = composition,
        progress = { progress },
        contentScale = ContentScale.Fit,
        modifier = Modifier
          .size(360.dp)
      )
      Text(
        text = stringResource(
          id = when (pageType) {
            PageType.Timeline -> R.string.empty_timeline
            PageType.Profile -> R.string.empty_status
          }
        ),
        fontWeight = FontWeight.Medium,
        color = AppTheme.colors.cardMenu,
        fontSize = 18.sp,
      )
    }
  }
}

enum class PageType {
  Timeline, Profile
}
