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

package com.github.whitescent.mastify.ui.component.status.action

import androidx.compose.animation.animateColorAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.github.whitescent.R
import com.github.whitescent.mastify.ui.component.ClickableIcon
import com.github.whitescent.mastify.ui.theme.AppTheme

@Composable
fun BookmarkButton(
  bookmarked: Boolean,
  modifier: Modifier = Modifier,
  unbookmarkedColor: Color = AppTheme.colors.primaryContent,
  onClick: (Boolean) -> Unit,
) {
  var bookmarkState by remember(bookmarked) { mutableStateOf(bookmarked) }
  val animatedIconColor by animateColorAsState(
    targetValue = if (bookmarkState) Color(0xFF498AE0) else unbookmarkedColor,
  )
  ClickableIcon(
    painter = painterResource(
      id = when (bookmarkState) {
        true -> R.drawable.bookmark_fill
        else -> R.drawable.bookmark_simple
      }
    ),
    modifier = modifier,
    tint = animatedIconColor,
  ) {
    bookmarkState = !bookmarkState
    onClick(bookmarkState)
  }
}
