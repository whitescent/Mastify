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

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.github.whitescent.mastify.ui.component.CenterRow
import com.github.whitescent.mastify.ui.theme.AppTheme
import com.github.whitescent.mastify.utils.PostState

@Composable
fun FollowButton(
  followed: Boolean,
  modifier: Modifier = Modifier,
  postState: PostState,
  requested: Boolean? = null,
  shape: Shape = AppTheme.shape.smallAvatar,
  onClick: (Boolean) -> Unit,
  content: @Composable RowScope.() -> Unit
) {
  val context = LocalContext.current
  Box(
    modifier = modifier
      .defaultMinSize(
        minWidth = ButtonDefaults.MinWidth,
        minHeight = 36.dp
      )
      .background(
        color = when (requested == null) {
          true -> {
            when (followed) {
              true -> AppTheme.colors.unfollowButtonBackground
              else -> AppTheme.colors.followButtonBackground
            }
          }
          else -> {
            when (requested) {
              true -> Color(0xFF596576)
              else -> {
                when (followed) {
                  true -> AppTheme.colors.unfollowButtonBackground
                  else -> AppTheme.colors.followButtonBackground
                }
              }
            }
          }
        },
        shape = shape,
      )
      .clip(shape)
      .clickable {
        onClick(!followed)
      },
    contentAlignment = Alignment.Center
  ) {
    CenterRow(Modifier.padding(10.dp)) {
      when (postState == PostState.Posting) {
        true -> CircularProgressIndicator(
          color = Color.White,
          modifier = Modifier.size(24.dp),
          strokeWidth = 1.5.dp
        )
        else -> content(this)
      }
    }
  }
  LaunchedEffect(postState) {
    if (postState is PostState.Failure) {
      Toast.makeText(context, "Failed to follow ${postState.throwable.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
  }
}
