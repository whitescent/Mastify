package com.github.whitescent.mastify.core.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

@Stable
class DialogState(
  initialShow: Boolean = false
) {

  var show by mutableStateOf(initialShow)
    private set

  fun show() { show = true }

  fun close() { show = false }

  companion object {
    val saver = mapSaver(
      save = {
        mapOf("show" to it.show)
      },
      restore = { map ->
        DialogState(map["show"] as? Boolean ?: false)
      }
    )
  }
}

@Composable
fun rememberDialogState(
  initialShow: Boolean = false
): DialogState = rememberSaveable(saver = DialogState.saver) {
  DialogState(initialShow)
}
