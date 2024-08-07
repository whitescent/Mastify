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

package com.github.whitescent.mastify.ui.component.status

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.whitescent.R
import com.github.whitescent.mastify.ui.component.CenterRow
import com.github.whitescent.mastify.ui.component.WidthSpacer
import com.github.whitescent.mastify.utils.thenIf
import kotlinx.coroutines.delay

@Composable
fun StatusSnackBar(
  snackbarState: StatusSnackbarState,
  modifier: Modifier = Modifier,
) {
  val data = snackbarState.current
  val isSwitching = snackbarState.isSwitching
  AnimatedContent(
    targetState = data,
    transitionSpec = {
      ContentTransform(
        targetContentEnter = slideInVertically { it } + fadeIn(tween(if (isSwitching) 300 else 400)),
        initialContentExit = slideOutVertically(tween(260), targetOffsetY = { it }) + fadeOut(),
        sizeTransform = null
      )
    },
    modifier = Modifier.fillMaxWidth()
      .thenIf(data != null) { modifier }
  ) {
    it?.let { type ->
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = when (type) {
          is StatusSnackbarType.Text -> Color(0xFF35465E)
          is StatusSnackbarType.Link -> Color(0xFF1B7CFF)
          is StatusSnackbarType.Bookmark -> Color(0xFF498AE0)
          is StatusSnackbarType.Error -> Color(0xFFF53232)
        },
        contentColor = Color.White,
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
      ) {
        CenterRow(Modifier.padding(horizontal = 22.dp, vertical = 16.dp)) {
          Icon(
            painter = painterResource(
              id = when (type) {
                is StatusSnackbarType.Text -> R.drawable.copy_fill
                is StatusSnackbarType.Link -> R.drawable.link_simple
                is StatusSnackbarType.Bookmark -> R.drawable.bookmark_fill
                is StatusSnackbarType.Error -> R.drawable.cloud_warning
              }
            ),
            contentDescription = null,
            modifier = Modifier.size(24.dp)
          )
          WidthSpacer(value = 8.dp)
          val snackbarMessage = when (type) {
            is StatusSnackbarType.Text -> stringResource(R.string.text_copied)
            is StatusSnackbarType.Link -> stringResource(R.string.link_copied)
            is StatusSnackbarType.Bookmark -> stringResource(R.string.bookmarked_snackBar)
            is StatusSnackbarType.Error -> {
              type.message ?: stringResource(id = R.string.load_post_error)
            }
          }
          Text(
            text = snackbarMessage,
            fontSize = 16.sp,
          )
        }
      }
    }
  }
  LaunchedEffect(data) {
    if (data != null) {
      delay(2500)
      snackbarState.dismiss()
    }
  }
}
