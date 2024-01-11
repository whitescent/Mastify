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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

class MastifyTypography {

  val statusDisplayName @Composable
  get() = TextStyle(
    fontSize = 15.sp,
    fontWeight = FontWeight(650),
    color = AppTheme.colors.primaryContent
  )

  val statusUsername @Composable
  get() = TextStyle(
    fontSize = 12.sp,
    color = AppTheme.colors.primaryContent.copy(alpha = 0.5f)
  )

  val statusRepost @Composable
  get() = TextStyle(
    fontSize = 14.sp,
    fontWeight = FontWeight.Normal,
    color = AppTheme.colors.primaryContent
  )

  val statusActions @Composable
  get() = TextStyle(
    fontSize = 12.sp,
    color = AppTheme.colors.cardAction
  )
}

val LocalMastifyTypography = staticCompositionLocalOf {
  MastifyTypography()
}
