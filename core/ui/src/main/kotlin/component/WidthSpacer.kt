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

package com.github.whitescent.mastify.core.ui.component

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

<<<<<<<< HEAD:core/ui/src/main/kotlin/component/Modifier.kt
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
========
@Composable
fun WidthSpacer(
  value: Dp
) = Spacer(Modifier.padding(horizontal = value))
>>>>>>>> cc21888 (refactor: refactor the entire project into multiple modules):core/ui/src/main/kotlin/component/WidthSpacer.kt
