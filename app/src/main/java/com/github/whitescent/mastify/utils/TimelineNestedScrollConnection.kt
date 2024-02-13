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

package com.github.whitescent.mastify.utils

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun rememberTimelineNestedScrollConnectionState(
  scrollThreshold: Float
): TimelineNestedScrollConnection {
  val scope = rememberCoroutineScope()
  val saver = remember { TimelineNestedScrollConnection.Saver(scope) }
  return rememberSaveable(scrollThreshold, saver = saver) {
    TimelineNestedScrollConnection(scope, scrollThreshold)
  }
}

@Stable
class TimelineNestedScrollConnection(
  private val scope: CoroutineScope,
  private val scrollThreshold: Float
) {

  var offset = Animatable(0f)

  val nestedScrollConnection = object : NestedScrollConnection {
    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
      return when {
        available.y >= 0 -> Offset.Zero
        offset.value == -scrollThreshold -> Offset.Zero
        else -> calculateOffset(available.y)
      }
    }

    override fun onPostScroll(
      consumed: Offset,
      available: Offset,
      source: NestedScrollSource,
    ): Offset {
      return when {
        available.y <= 0 -> Offset.Zero
        offset.value == 0f -> Offset.Zero
        else -> calculateOffset(available.y)
      }
    }
  }

  fun calculateOffset(delta: Float): Offset {
    return if (delta < 0 && offset.value > -scrollThreshold || delta > 0 && offset.value < 0f) {
      scope.launch {
        offset.snapTo((offset.value + delta).coerceIn(-scrollThreshold, 0f))
      }
      Offset(0f, delta)
    } else Offset.Zero
  }

  companion object {
    fun Saver(
      scope: CoroutineScope,
    ): Saver<TimelineNestedScrollConnection, *> = mapSaver(
      save = {
        mapOf("scrollThreshold" to it.scrollThreshold)
      },
      restore = {
        TimelineNestedScrollConnection(
          scope = scope,
          scrollThreshold = it["scrollThreshold"] as Float
        )
      }
    )
  }
}
