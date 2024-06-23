package com.github.whitescent.mastify.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.github.whitescent.mastify.core.ui.AppTheme

@Composable
fun BasicDialog(
  dialogState: DialogState,
  properties: DialogProperties = DialogProperties(),
  content: @Composable () -> Unit
) {
  if (dialogState.show) {
    Dialog(
      onDismissRequest = dialogState::close,
      properties = properties,
      content = {
        Box(
          modifier = Modifier
            .background(AppTheme.colors.background, AppTheme.shape.smallAvatar)
            .clip(AppTheme.shape.smallAvatar)
        ) {
          content()
        }
      }
    )
  }
}
