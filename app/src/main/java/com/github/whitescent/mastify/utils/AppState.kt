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

package com.github.whitescent.mastify.utils

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

@Stable
class AppState(
  private val appContentPaddingTop: Dp,
  private val appContentPaddingBottom: Dp,
) {

  private val scrollToTopChannel = Channel<Unit>()
  val scrollToTopFlow = scrollToTopChannel.receiveAsFlow()

  var appPaddingValues by mutableStateOf(
    PaddingValues(top = appContentPaddingTop, bottom = appContentPaddingBottom)
  )
    private set

  fun setPaddingValues(paddingValues: PaddingValues) {
    appPaddingValues = paddingValues
  }

  suspend fun scrollToTop() { scrollToTopChannel.send(Unit) }

  companion object {
    val saver = listSaver(
      save = { listOf(it.appContentPaddingTop.value, it.appContentPaddingBottom.value) },
      restore = { AppState(Dp(it[0]), Dp(it[1])) }
    )
  }
}

@Composable
fun rememberAppState(topPadding: Dp = 0.dp, bottomPadding: Dp = 0.dp): AppState {
  return rememberSaveable(saver = AppState.saver) { AppState(topPadding, bottomPadding) }
}
