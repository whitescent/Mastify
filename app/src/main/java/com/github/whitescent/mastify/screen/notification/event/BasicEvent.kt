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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.whitescent.R
import com.github.whitescent.mastify.data.model.ui.StatusUiData
import com.github.whitescent.mastify.network.model.account.Account
import com.github.whitescent.mastify.network.model.notification.Notification
import com.github.whitescent.mastify.network.model.status.Status
import com.github.whitescent.mastify.ui.component.CenterRow
import com.github.whitescent.mastify.ui.component.CircleShapeAsyncImage
import com.github.whitescent.mastify.ui.component.HeightSpacer
import com.github.whitescent.mastify.ui.component.HtmlText
import com.github.whitescent.mastify.ui.component.LocalizedAnnotatedText
import com.github.whitescent.mastify.ui.component.WidthSpacer
import com.github.whitescent.mastify.ui.theme.AppTheme
import com.github.whitescent.mastify.utils.getRelativeTimeSpanString
import kotlinx.datetime.Clock
import kotlinx.datetime.toInstant

@Composable
fun BasicEvent(
  event: Notification.Type,
  actionAccount: Account,
  status: StatusUiData,
  modifier: Modifier = Modifier,
  navigateToDetail: (Status) -> Unit,
  navigateToProfile: (Account) -> Unit
) {
  val context = LocalContext.current
  Row(modifier.fillMaxWidth()) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      CircleShapeAsyncImage(
        model = actionAccount.avatar,
        modifier = Modifier.size(36.dp)
      ) {
        navigateToProfile(actionAccount)
      }
      HeightSpacer(value = 6.dp)
      Icon(
        painter = painterResource(
          id = when (event) {
            is Notification.Type.Favourite -> R.drawable.heart_fill
            is Notification.Type.Reblog -> R.drawable.repeat
            is Notification.Type.Mention -> R.drawable.at_bold
            else -> throw IllegalArgumentException()
          }
        ),
        contentDescription = null,
        tint = when (event) {
          is Notification.Type.Favourite -> AppTheme.colors.cardLike
          is Notification.Type.Reblog -> AppTheme.colors.reblogged
          is Notification.Type.Mention -> Color(0xFFA55FFF)
          else -> throw IllegalArgumentException()
        },
        modifier = Modifier.size(20.dp),
      )
    }
    WidthSpacer(value = 8.dp)
    Column {
      CenterRow {
        LocalizedAnnotatedText(
          stringRes = when (event) {
            is Notification.Type.Favourite -> R.string.someone_liked_your_post
            is Notification.Type.Reblog -> R.string.someone_boosted_your_post
            is Notification.Type.Mention -> R.string.someone_mentioned_you
            else -> throw IllegalArgumentException()
          },
          emojis = actionAccount.emojis,
          highlightText = actionAccount.realDisplayName,
          allowHighLightClick = false,
          highlightSpanStyle = SpanStyle(
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = AppTheme.colors.primaryContent
          ),
          fontSize = 15.sp,
          modifier = Modifier.weight(1f),
          color = AppTheme.colors.primaryContent.copy(.65f),
          maxLines = 1
        )
        WidthSpacer(value = 6.dp)
        Text(
          text = remember(status.createdAt) {
            getRelativeTimeSpanString(
              context,
              status.createdAt.toInstant().toEpochMilliseconds(),
              Clock.System.now().toEpochMilliseconds()
            )
          },
          style = AppTheme.typography.statusUsername.copy(
            color = AppTheme.colors.primaryContent.copy(alpha = 0.48f),
          )
        )
      }
      HeightSpacer(value = 4.dp)
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(AppTheme.shape.smallAvatar)
          .clickable {
            navigateToDetail(status.actionable)
          }
          .background(
            color = if (event !is Notification.Type.Mention) Color(0xFFF5F5F5) else Color(0xFFA55FFF).copy(.13f),
            shape = AppTheme.shape.smallAvatar
          )
      ) {
        HtmlText(
          text = status.content,
          color = AppTheme.colors.primaryContent,
          maxLines = 6,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.padding(10.dp),
          filterMentionText = status.isInReplyToSomeone
        )
      }
    }
  }
}
