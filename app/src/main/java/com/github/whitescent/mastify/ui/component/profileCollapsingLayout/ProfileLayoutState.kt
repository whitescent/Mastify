package com.github.whitescent.mastify.ui.component.profileCollapsingLayout

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun rememberProfileLayoutState(): ProfileLayoutState {
  val scope = rememberCoroutineScope()
  val saver = remember { ProfileLayoutState.Saver(scope) }
  return rememberSaveable(saver = saver) {
    ProfileLayoutState(scope)
  }
}

@Stable
class ProfileLayoutState(
  private val scope: CoroutineScope,
  initialOffset: Float = 0f,
  initialMaxOffset: Float = 0f,
) {

  var maxOffset by mutableFloatStateOf(initialMaxOffset)
    private set
  var bodyContentMaxHeight by mutableStateOf(Dp.Unspecified)

  var offset = Animatable(initialOffset)

  val progress by derivedStateOf {
    abs(offset.value / maxOffset)
  }

  val nestedScrollConnection = object : NestedScrollConnection {
    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
      return when {
        available.y <= 0 -> Offset(0f, calculateOffset(available.y))
        else -> Offset.Zero
      }
    }

    override fun onPostScroll(
      consumed: Offset,
      available: Offset,
      source: NestedScrollSource,
    ): Offset {
      return when {
        available.y >= 0 -> Offset(0f, calculateOffset(available.y))
        else -> Offset.Zero
      }
    }
  }

  fun calculateOffset(delta: Float): Float {
    return if (delta < 0 && offset.value > -maxOffset || delta > 0 && offset.value < 0f) {
      scope.launch {
        offset.snapTo((offset.value + delta).coerceIn(-maxOffset, 0f))
      }
      delta
    } else {
      0f
    }
  }

  fun updateBounds(maxOffset: Float) {
    this.maxOffset = maxOffset
  }

  fun animatedToTop() {
    scope.launch {
      offset.animateTo(0f)
    }
  }

  companion object {
    fun Saver(
      scope: CoroutineScope,
    ): Saver<ProfileLayoutState, *> = listSaver(
      save = {
        listOf(it.offset.value, it.maxOffset)
      },
      restore = {
        ProfileLayoutState(
          scope = scope,
          initialOffset = it[0],
          initialMaxOffset = it[1],
        )
      }
    )
  }
}
