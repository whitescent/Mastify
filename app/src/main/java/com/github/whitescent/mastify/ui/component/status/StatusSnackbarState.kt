/*
 * Copyright 2023 WhiteScent
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

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.resume

@Stable
class StatusSnackbarState {

  private val mutex = Mutex()

  var currentSnackbarData by mutableStateOf<StatusSnackbarData?>(null)
    private set

  suspend fun showSnackbar(
    snackbarType: StatusSnackbarType
  ): StatusSnackbarResult = mutex.withLock {
    try {
      return suspendCancellableCoroutine { continuation ->
        currentSnackbarData = StatusSnackbarStateImpl(snackbarType, continuation)
      }
    } finally {
      currentSnackbarData = null
    }
  }

  private class StatusSnackbarStateImpl(
    override val type: StatusSnackbarType,
    private val continuation: CancellableContinuation<StatusSnackbarResult>
  ) : StatusSnackbarData {
    override fun dismiss() {
      if (continuation.isActive) continuation.resume(StatusSnackbarResult.Dismissed)
    }
  }
}

@Stable
interface StatusSnackbarData {
  val type: StatusSnackbarType
  fun dismiss()
}

enum class StatusSnackbarType {
  Text, Link, Bookmark, Error
}

enum class StatusSnackbarResult {
  Dismissed
}
