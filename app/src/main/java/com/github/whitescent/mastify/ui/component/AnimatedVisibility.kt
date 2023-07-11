@file:Suppress("UnusedReceiverParameter")

package com.github.whitescent.mastify.ui.component

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun BoxScope.AnimatedVisibility(
  visible: Boolean,
  modifier: Modifier = Modifier,
  enter: EnterTransition = fadeIn() + expandIn(),
  exit: ExitTransition = shrinkOut() + fadeOut(),
  label: String = "AnimatedVisibility",
  content: @Composable() AnimatedVisibilityScope.() -> Unit
) = androidx.compose.animation.AnimatedVisibility(visible, modifier, enter, exit, label, content)
