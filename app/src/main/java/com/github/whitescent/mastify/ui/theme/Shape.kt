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

package com.github.whitescent.mastify.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp
import com.github.whitescent.mastify.ui.theme.shape.SmoothCornerShape

class MastifyShape {
  val smallAvatar @Composable get() = SmoothCornerShape(10.dp)
  val betweenSmallAndMediumAvatar @Composable get() = SmoothCornerShape(12.dp)
  val mediumAvatar @Composable get() = SmoothCornerShape(20.dp)
  val largeAvatar @Composable get() = SmoothCornerShape(25.dp)
  val normal @Composable get() = SmoothCornerShape(15.dp)
  val voteOption @Composable get() = SmoothCornerShape(16.dp)
}

val LocalMastifyShape = staticCompositionLocalOf {
  MastifyShape()
}
