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

  fun showDialog() { show = true }

  fun closeDialog() { show = false }

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
