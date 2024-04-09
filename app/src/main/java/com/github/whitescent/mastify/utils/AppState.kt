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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

@Stable
class AppState(
  unread: Int
) {

  private val scrollToTopChannel = MutableSharedFlow<Unit>()
  val scrollToTopFlow = scrollToTopChannel.asSharedFlow()

  var hideBottomBar by mutableStateOf(false)

  var unreadNotifications by mutableIntStateOf(unread)

  suspend fun scrollToTop() { scrollToTopChannel.emit(Unit) }

  companion object {
    val saver = mapSaver(
      save = {
        mapOf("unread" to it.unreadNotifications)
      },
      restore = {
        AppState(it["unread"] as Int)
      }
    )
  }
}

@Composable
fun rememberAppState(unread: Int = 0): AppState {
  return rememberSaveable(saver = AppState.saver) {
    AppState(unread)
  }
}
