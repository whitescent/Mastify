package com.github.whitescent.mastify.ui.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle

@Composable
fun AnimatedText(
  text: String,
  modifier: Modifier = Modifier,
  style: TextStyle = LocalTextStyle.current
) =
  AnimatedContent(
    targetState = text,
    transitionSpec = {
      if (targetState > initialState) {
        (slideInVertically { height -> height } + fadeIn())
          .togetherWith(slideOutVertically { height -> -height } + fadeOut())
      } else {
        (slideInVertically { height -> -height } + fadeIn())
          .togetherWith(slideOutVertically { height -> height } + fadeOut())
      }.using(
        SizeTransform(clip = false)
      )
    },
    label = "",
    modifier = modifier
  ) { targetText ->
    Text(text = targetText, style = style)
  }
