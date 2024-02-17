/*
 * Copyright 2024 WhiteScent
 *
 * This file is a part of Mastify.
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * Mastify is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Mastify; if not,
 * see <http://www.gnu.org/licenses>.
 */

package com.github.whitescent.mastify.ui.component.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.github.whitescent.mastify.ui.theme.AppTheme

@Composable
fun BasicDialog(
  dialogState: DialogState,
  properties: DialogProperties = DialogProperties(),
  content: @Composable () -> Unit
) {
  if (dialogState.show) {
    Dialog(
      onDismissRequest = dialogState::closeDialog,
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
