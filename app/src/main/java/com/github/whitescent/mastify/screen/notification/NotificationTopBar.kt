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

package com.github.whitescent.mastify.screen.notification

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.whitescent.R
import com.github.whitescent.mastify.database.model.AccountEntity
import com.github.whitescent.mastify.ui.component.CenterRow
import com.github.whitescent.mastify.ui.component.CircleShapeAsyncImage
import com.github.whitescent.mastify.ui.component.ClickableIcon
import com.github.whitescent.mastify.ui.component.WidthSpacer
import com.github.whitescent.mastify.ui.theme.AppTheme

@Composable
fun NotificationTopBar(
  activeAccount: AccountEntity,
  modifier: Modifier = Modifier,
  dismissAllNotification: () -> Unit,
  openDrawer: () -> Unit,
) {
  CenterRow(modifier.fillMaxWidth()) {
    CenterRow(Modifier.weight(1f)) {
      CircleShapeAsyncImage(
        model = activeAccount.profilePictureUrl,
        modifier = Modifier
          .size(36.dp)
          .shadow(12.dp, AppTheme.shape.betweenSmallAndMediumAvatar),
        shape = AppTheme.shape.betweenSmallAndMediumAvatar,
        onClick = openDrawer
      )
      WidthSpacer(value = 6.dp)
      Text(
        text = stringResource(R.string.notifications_title),
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = AppTheme.colors.primaryContent,
        modifier = Modifier.weight(1f),
      )
    }
    ClickableIcon(
      painter = painterResource(id = R.drawable.checks_bold),
      tint = AppTheme.colors.primaryContent,
      modifier = Modifier.size(24.dp),
    ) {
      dismissAllNotification()
    }
  }
}
