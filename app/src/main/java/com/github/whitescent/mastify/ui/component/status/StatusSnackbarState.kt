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
