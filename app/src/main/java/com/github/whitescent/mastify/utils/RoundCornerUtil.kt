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

import android.os.Build
import android.view.RoundedCorner
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import kotlin.math.ceil

@Composable
fun Int.roundToDp(): Dp {
  val num = this
  return with(LocalDensity.current) {
    ceil(num.toDp().value.toDouble()).dp
  }
}

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun windowCornerRadius(position: Int): Dp {
  return (LocalView.current.rootWindowInsets?.getRoundedCorner(position)?.radius ?: 0).roundToDp()
}

@Composable
fun windowBottomStartCornerRadius(): Dp {
  if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return 0.dp
  return if (LocalConfiguration.current.layoutDirection == ViewCompat.LAYOUT_DIRECTION_LTR) {
    windowCornerRadius(position = RoundedCorner.POSITION_BOTTOM_LEFT)
  } else {
    windowCornerRadius(position = RoundedCorner.POSITION_BOTTOM_RIGHT)
  }
}

@Composable
fun windowBottomEndCornerRadius(): Dp {
  if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return 0.dp
  return if (LocalConfiguration.current.layoutDirection == ViewCompat.LAYOUT_DIRECTION_LTR) {
    windowCornerRadius(position = RoundedCorner.POSITION_BOTTOM_RIGHT)
  } else {
    windowCornerRadius(position = RoundedCorner.POSITION_BOTTOM_LEFT)
  }
}
