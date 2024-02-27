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

package com.github.whitescent.mastify.screen.notification.event

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.whitescent.R
import com.github.whitescent.mastify.network.model.account.Account
import com.github.whitescent.mastify.network.model.notification.Notification
import com.github.whitescent.mastify.network.model.notification.Notification.Type.SpecialEvent
import com.github.whitescent.mastify.ui.component.CenterRow
import com.github.whitescent.mastify.ui.component.CircleShapeAsyncImage
import com.github.whitescent.mastify.ui.component.LocalizedAnnotatedText
import com.github.whitescent.mastify.ui.component.WidthSpacer
import com.github.whitescent.mastify.ui.theme.AppTheme
import com.github.whitescent.mastify.utils.clickableWithoutIndication

@Composable
fun FollowEvent(
  event: SpecialEvent,
  actionAccount: Account,
  modifier: Modifier = Modifier,
  navigateToDetail: () -> Unit,
  navigateToProfile: (Account) -> Unit,
  acceptRequest: (String) -> Unit,
  rejectRequest: (String) -> Unit,
) {
  CenterRow(
    modifier = modifier
      .fillMaxWidth()
      .clickableWithoutIndication {
        when (event !is Notification.Type.Poll) {
          true -> navigateToProfile(actionAccount)
          else -> navigateToDetail()
        }
      }
  ) {
    CenterRow {
      CenterRow(Modifier.weight(1f)) {
        Box {
          CircleShapeAsyncImage(
            model = actionAccount.avatar,
            modifier = Modifier.size(36.dp)
          ) {
            navigateToProfile(actionAccount)
          }
          Image(
            painter = painterResource(
              when (event !is Notification.Type.Poll) {
                true -> R.drawable.user_follow_circle
                else -> R.drawable.poll_circle
              }
            ),
            contentDescription = null,
            modifier = Modifier
              .size(20.dp)
              .align(Alignment.BottomEnd)
              .offset(10.dp, 10.dp)
          )
        }
        WidthSpacer(value = 8.dp)
        LocalizedAnnotatedText(
          stringRes = when (event) {
            is Notification.Type.Follow -> R.string.someone_followed_you
            is Notification.Type.FollowRequest -> R.string.someone_request_follow_you
            is Notification.Type.Poll -> R.string.someone_poll_end
          },
          emojis = actionAccount.emojis,
          highlightText = actionAccount.realDisplayName,
          highlightSpanStyle = SpanStyle(
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = AppTheme.colors.primaryContent
          ),
          fontSize = 16.sp,
          modifier = Modifier.weight(1f),
          color = AppTheme.colors.primaryContent.copy(.65f),
          maxLines = 1
        )
      }
      WidthSpacer(value = 6.dp)
      when (event) {
        is Notification.Type.FollowRequest -> {
          CenterRow {
            IconButton(
              onClick = { rejectRequest(actionAccount.id) }
            ) {
              Image(
                painter = painterResource(id = R.drawable.close_circle),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
              )
            }
            IconButton(
              onClick = { acceptRequest(actionAccount.id) }
            ) {
              Image(
                painter = painterResource(id = R.drawable.check_circle_fill),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
              )
            }
          }
        }
        is Notification.Type.Poll -> {
          Icon(
            painter = painterResource(id = R.drawable.right_arrow),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = AppTheme.colors.primaryContent
          )
        }
        else -> Unit
      }
    }
  }
}
