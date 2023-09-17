package com.github.whitescent.mastify.ui.component.status.paging

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.github.whitescent.R
import com.github.whitescent.mastify.ui.component.HeightSpacer

@Composable
fun StatusListLoadError(retry: () -> Unit) {
  val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.error))
  val progress by animateLottieCompositionAsState(
    composition = composition,
    iterations = LottieConstants.IterateForever
  )
  Box(
    modifier = Modifier.fillMaxSize(),
    contentAlignment = Alignment.Center
  ) {
    Column(
      modifier = Modifier.fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = Modifier.size(160.dp)
      )
      HeightSpacer(value = 4.dp)
      Text(
        text = "获取嘟文失败... :(",
        fontWeight = FontWeight.Bold
      )
      HeightSpacer(value = 4.dp)
      Button(
        onClick = retry,
      ) {
        Text(
          text = "重新获取",
        )
      }
    }
  }
}
