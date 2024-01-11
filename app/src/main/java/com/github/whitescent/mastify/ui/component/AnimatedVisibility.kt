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

@file:Suppress("UnusedReceiverParameter")

package com.github.whitescent.mastify.ui.component

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun BoxScope.AnimatedVisibility(
  visible: Boolean,
  modifier: Modifier = Modifier,
  enter: EnterTransition = fadeIn() + expandIn(),
  exit: ExitTransition = shrinkOut() + fadeOut(),
  label: String = "AnimatedVisibility",
  content: @Composable() AnimatedVisibilityScope.() -> Unit
) = androidx.compose.animation.AnimatedVisibility(visible, modifier, enter, exit, label, content)
