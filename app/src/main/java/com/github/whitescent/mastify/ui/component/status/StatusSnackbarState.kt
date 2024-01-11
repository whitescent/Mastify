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

package com.github.whitescent.mastify.ui.component.status

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Stable
class StatusSnackbarState {

  private var previous: StatusSnackbarType? = null

  var current by mutableStateOf<StatusSnackbarType?>(null)
    private set

  val isSwitching = previous != null && previous != current && current != null

  fun show(snackbarType: StatusSnackbarType) {
    previous = current
    current = snackbarType
  }

  fun dismiss() {
    current = null
  }
}

enum class StatusSnackbarType {
  Text, Link, Bookmark, Error
}

@Composable
fun rememberStatusSnackBarState(): StatusSnackbarState = remember {
  StatusSnackbarState()
}
