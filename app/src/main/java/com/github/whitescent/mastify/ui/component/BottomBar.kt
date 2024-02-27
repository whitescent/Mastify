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

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.github.whitescent.mastify.screen.destinations.Destination
import com.github.whitescent.mastify.ui.theme.AppTheme
import com.github.whitescent.mastify.utils.AppState
import com.github.whitescent.mastify.utils.BottomBarItem
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
        NavigationBarItem(
          selected = selected,
          onClick = {
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
          },
          icon = {
            BadgedBox(
              badge = {
                if (appState.unreadNotifications > 0 && screen == BottomBarItem.Notification) {
                  Badge(
                    containerColor = Color(0xFFFF0000),
                    contentColor = Color.White
                  ) {
                    Text("${appState.unreadNotifications}")
                  }
                }
              }
            ) {
              BottomBarIcon(
                icon = screen.icon,
                selected = destination == screen.direction,
                modifier = Modifier
              )
            }
          },
          colors = NavigationBarItemDefaults.colors(
            indicatorColor = Color.Transparent,
            selectedIconColor = AppTheme.colors.primaryContent
          ),
        )
      }
    }
  }
}

@Composable
private fun BottomBarIcon(
  @DrawableRes icon: Int,
  selected: Boolean,
  modifier: Modifier = Modifier
) {
  Icon(
    painter = painterResource(icon),
    contentDescription = null,
    modifier = modifier
      .size(24.dp)
      .let {
        if (!selected) it.alpha(0.2f) else it
      },
    tint = AppTheme.colors.primaryContent
  )
}
