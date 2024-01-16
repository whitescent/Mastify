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

package com.github.whitescent.mastify.screen.post

import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.github.whitescent.R
import com.github.whitescent.mastify.ui.component.CenterRow
import com.github.whitescent.mastify.ui.component.WidthSpacer
import com.github.whitescent.mastify.ui.theme.AppTheme
import com.github.whitescent.mastify.utils.PostState
import com.github.whitescent.mastify.utils.PostState.Failure
import com.github.whitescent.mastify.utils.PostState.Idle
import com.github.whitescent.mastify.utils.PostState.Posting
import com.github.whitescent.mastify.utils.PostState.Success

@Composable
fun PostButton(
  enabled: Boolean,
  postState: PostState,
  post: () -> Unit,
) {
  val context = LocalContext.current
  Box(
    modifier = Modifier
      .background(
        color = if (enabled) AppTheme.colors.accent else Color.Gray,
        shape = AppTheme.shape.mediumAvatar
      )
      .clip(AppTheme.shape.mediumAvatar)
      .clickable(
        enabled = enabled,
        onClick = post
      )
      .animateContentSize()
  ) {
    when (postState) {
      Idle, Success, is Failure -> {
        CenterRow(Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
          Icon(
            painter = painterResource(id = R.drawable.send),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = Color.White
          )
          WidthSpacer(value = 4.dp)
          Text(
            text = "Create",
            color = Color.White
          )
        }
      }
      Posting -> CircularProgressIndicator(color = Color.White)
    }
  }
  LaunchedEffect(postState) {
    if (postState is Failure) {
      Toast.makeText(context, postState.throwable.localizedMessage, Toast.LENGTH_LONG).show()
    }
  }
}
