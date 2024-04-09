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

package com.github.whitescent.mastify.ui.component

import android.view.animation.OvershootInterpolator
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.github.whitescent.mastify.screen.destinations.Destination
import com.github.whitescent.mastify.ui.component.foundation.Text
import com.github.whitescent.mastify.ui.theme.AppTheme
import com.github.whitescent.mastify.ui.theme.shape.SmoothCornerShape
import com.github.whitescent.mastify.utils.AppState
import com.github.whitescent.mastify.utils.BottomBarItem
import com.github.whitescent.mastify.utils.clickableWithoutIndication
import com.ramcosta.composedestinations.navigation.navigate

@Composable
fun BottomBar(
  appState: AppState,
  navController: NavController,
  destination: Destination,
  scrollToTop: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier
      .fillMaxWidth()
      .shadow(24.dp, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
    color = AppTheme.colors.bottomBarBackground,
  ) {
    CenterRow(Modifier.navigationBarsPadding()) {
      BottomBarItem.entries.forEachIndexed { _, screen ->
        val selected = destination.route == screen.direction.route
        BottomBarIcon(
          icon = {
            AnimatedContent(
              targetState = selected,
              transitionSpec = {
                ContentTransform(
                  targetContentEnter = scaleIn(
                    animationSpec = tween(
                      durationMillis = 340,
                      easing = overshootEasing()
                    ),
                  ),
                  initialContentExit = fadeOut(tween(277)),
                ).using(SizeTransform(clip = false))
              },
            ) { isSelected ->
              Icon(
                painter = painterResource(screen.icon),
                contentDescription = null,
                modifier = modifier
                  .size(24.dp)
                  .let {
                    if (!isSelected) it.alpha(0.2f) else it
                  },
                tint = AppTheme.colors.primaryContent
              )
            }
          },
          unreadBubble = {
            Box(
              modifier = Modifier
                .padding(4.dp)
                .sizeIn(20.dp, 20.dp, maxHeight = 20.dp)
                .clip(SmoothCornerShape(6.dp))
                .background(AppTheme.colors.primaryGradient, SmoothCornerShape(5.dp)),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = appState.unreadNotifications.toString(),
                color = Color.White,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
              )
            }
          },
          modifier = Modifier.padding(24.dp),
        ) {
          when (selected) {
            true -> scrollToTop()
            false -> {
              navController.navigate(screen.direction) {
                popUpTo(destination.route) {
                  saveState = true
                  inclusive = true
                }
                restoreState = true
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun RowScope.BottomBarIcon(
  icon: @Composable () -> Unit,
  unreadBubble: @Composable BoxScope.() -> Unit,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  Box(
    modifier = modifier
      .weight(1f)
      .clickableWithoutIndication { onClick() },
    contentAlignment = Alignment.Center
  ) {
    BadgedBox(
      badge = {
        unreadBubble()
      },
    ) {
      icon()
    }
  }
}

private fun overshootEasing(tension: Float = 1.9f) = Easing {
  OvershootInterpolator(tension).getInterpolation(it)
}
