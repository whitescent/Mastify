package com.github.whitescent.mastify.feature.login

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.github.whitescent.mastify.core.common.strings.CommonStrings
import com.github.whitescent.mastify.core.ui.AppTheme
import com.github.whitescent.mastify.core.ui.component.BasicDialog
import com.github.whitescent.mastify.core.ui.component.CenterRow
import com.github.whitescent.mastify.core.ui.component.DialogState
import com.github.whitescent.mastify.core.ui.component.WidthSpacer

@Composable
fun LoginDialog(
  dialogState: DialogState
) = BasicDialog(
  dialogState = dialogState,
  properties = DialogProperties(
    usePlatformDefaultWidth = false,
    dismissOnClickOutside = false,
    dismissOnBackPress = false
  )
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = AppTheme.colors.background,
    shadowElevation = 6.dp
  ) {
    CenterRow(
      modifier = Modifier.padding(24.dp)
    ) {
      Text(
        text = stringResource(id = CommonStrings.title_connecting),
        color = AppTheme.colors.primaryContent
      )
      WidthSpacer(value = 16.dp)
      CircularProgressIndicator(
        modifier = Modifier.size(22.dp),
        strokeWidth = 3.dp,
        color = AppTheme.colors.accent
      )
    }
  }
}
