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

package com.github.whitescent.mastify.ui.component.button

import androidx.compose.animation.animateColorAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.github.whitescent.R
import com.github.whitescent.mastify.ui.component.ClickableIcon
import com.github.whitescent.mastify.ui.theme.AppTheme

@Composable
fun FavoriteButton(
  favorited: Boolean,
  modifier: Modifier = Modifier,
  unfavoritedColor: Color = AppTheme.colors.cardAction,
  onClick: (Boolean) -> Unit,
) {
  val animatedFavIconColor by animateColorAsState(
    targetValue = if (favorited) AppTheme.colors.cardLike else unfavoritedColor,
  )
  ClickableIcon(
    painter = painterResource(id = if (favorited) R.drawable.heart_fill else R.drawable.heart),
    modifier = modifier,
    tint = animatedFavIconColor,
  ) {
    onClick(!favorited)
  }
}
