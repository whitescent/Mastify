package com.github.whitescent.mastify.core.common.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow

@Composable
fun <T : Any> SingleEventEffect(
  sideEffectFlow: Flow<T>,
  lifeCycleState: Lifecycle.State = Lifecycle.State.STARTED,
  collector: (T) -> Unit
) {
  val lifecycleOwner = LocalLifecycleOwner.current
  val newCollector by rememberUpdatedState(collector)
  LaunchedEffect(sideEffectFlow) {
    lifecycleOwner.repeatOnLifecycle(lifeCycleState) {
      sideEffectFlow.collect(newCollector)
    }
  }
}
