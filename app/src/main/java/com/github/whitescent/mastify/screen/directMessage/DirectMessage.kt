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

package com.github.whitescent.mastify.screen.directMessage

import androidx.compose.runtime.Composable
import com.github.whitescent.mastify.AppNavGraph
import com.github.whitescent.mastify.ui.transitions.BottomBarScreenTransitions
import com.ramcosta.composedestinations.annotation.Destination

@Destination(style = BottomBarScreenTransitions::class)
@AppNavGraph
@Composable
fun DirectMessage() {
}
