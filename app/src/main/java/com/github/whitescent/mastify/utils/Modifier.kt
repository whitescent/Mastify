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

import androidx.compose.foundation.clickable
import androidx.compose.ui.Modifier

fun Modifier.clickableWithoutIndication(
  enabled: Boolean = true,
  onClick: () -> Unit
) = this.clickable(
  onClick = onClick,
  enabled = enabled,
  indication = null,
  interactionSource = null,
)

inline fun Modifier.thenIf(
  condition: Boolean,
  modifier: Modifier.() -> Modifier,
): Modifier = if (condition) modifier() else this
