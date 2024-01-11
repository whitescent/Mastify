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
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.EaseInOutQuad
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.whitescent.R
import com.github.whitescent.mastify.ui.component.CenterRow
import com.github.whitescent.mastify.ui.component.WidthSpacer
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
        targetContentEnter = slideInVertically(
          animationSpec = when (isSwitching) {
            // We want it to have some delay when switching so the old toast goes up first
            true -> tween(440, easing = CubicBezierEasing(0.56f, -0.41f, 0.4f, 1.4f))
            // Otherwise, we just use the normal animation
            false -> spring(
              stiffness = Spring.StiffnessMediumLow,
              visibilityThreshold = IntOffset.VisibilityThreshold
            )
          },
          initialOffsetY = { it }
        ) + fadeIn(tween(if (isSwitching) 300 else 400)),
        initialContentExit = when (isSwitching) {
          // We want to slide up the old toast when we switch to a new one
          true -> slideOutVertically(
            animationSpec = tween(480, easing = EaseInOutQuad),
            targetOffsetY = { -(it * 1.3).toInt() }
          ) + fadeOut(tween(480))
          // Otherwise, we just drop the toast down
          false -> slideOutVertically(tween(260), targetOffsetY = { it }) + fadeOut()
        },
      ).using(SizeTransform(clip = false))
    },
    modifier = modifier.fillMaxWidth()
  ) {
    it?.let { type ->
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = when (type) {
          StatusSnackbarType.Text -> Color(0xFF35465E)
          StatusSnackbarType.Link -> Color(0xFF1B7CFF)
          StatusSnackbarType.Bookmark -> Color(0xFF498AE0)
          StatusSnackbarType.Error -> Color(0xFFF53232)
        },
        contentColor = Color.White,
        shadowElevation = 4.dp,
        modifier = modifier.fillMaxWidth()
      ) {
        CenterRow(Modifier.padding(horizontal = 22.dp, vertical = 16.dp)) {
          Icon(
            painter = painterResource(
              id = when (type) {
                StatusSnackbarType.Text -> R.drawable.copy_fill
                StatusSnackbarType.Link -> R.drawable.link_simple
                StatusSnackbarType.Bookmark -> R.drawable.bookmark_fill
                StatusSnackbarType.Error -> R.drawable.cloud_warning
              }
            ),
            contentDescription = null,
            modifier = Modifier.size(24.dp)
          )
          WidthSpacer(value = 8.dp)
          Text(
            text = stringResource(
              when (type) {
                StatusSnackbarType.Text -> R.string.text_copied
                StatusSnackbarType.Link -> R.string.link_copied
                StatusSnackbarType.Bookmark -> R.string.bookmarked_snackBar
                StatusSnackbarType.Error -> R.string.load_post_error
              }
            ),
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
