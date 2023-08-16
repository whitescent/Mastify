package com.github.whitescent.mastify.ui.component.profileCollapsingLayout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import kotlin.math.abs

@Composable
fun rememberProfileLayoutState(): ProfileLayoutState {
  val saver = remember { ProfileLayoutState.Saver() }
  return rememberSaveable(saver = saver) {
    ProfileLayoutState()
  }
}

class ProfileLayoutState(
  initialOffset: Float = 0f,
  initialMaxOffset: Float = 0f,
) {

  private var maxOffset by mutableFloatStateOf(initialMaxOffset)
  var offset by mutableFloatStateOf(initialOffset)
    private set
  val progress get() = abs(offset / maxOffset)

  val nestedScrollConnection = object : NestedScrollConnection {
    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
      return when {
        available.y < 0 -> Offset(0f, calculateOffset(available.y))
        else -> Offset.Zero
      }
    }

    override fun onPostScroll(
      consumed: Offset,
      available: Offset,
      source: NestedScrollSource,
    ): Offset {
      return when {
        available.y > 0 -> Offset(0f, calculateOffset(available.y))
        else -> Offset.Zero
      }
    }
  }

  fun calculateOffset(delta: Float): Float {
    return if (delta < 0 && offset > -maxOffset || delta > 0 && offset < 0f) {
      offset = (offset + delta).coerceIn(-maxOffset, 0f)
      delta
    } else {
      0f
    }
  }

  fun updateBounds(maxOffset: Float) {
    this.maxOffset = maxOffset
  }

  companion object {
    fun Saver(): Saver<ProfileLayoutState, *> = listSaver(
      save = {
        listOf(it.offset, it.maxOffset)
      },
      restore = {
        ProfileLayoutState(
          initialOffset = it[0],
          initialMaxOffset = it[1],
        )
      }
    )
  }
}
