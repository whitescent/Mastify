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

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.whitescent.R
import com.github.whitescent.mastify.data.model.ui.StatusUiData
import com.github.whitescent.mastify.network.model.account.Account
import com.github.whitescent.mastify.network.model.status.Status.Application
import com.github.whitescent.mastify.network.model.status.Status.Attachment
import com.github.whitescent.mastify.ui.component.AnimatedText
import com.github.whitescent.mastify.ui.component.CenterRow
import com.github.whitescent.mastify.ui.component.CircleShapeAsyncImage
import com.github.whitescent.mastify.ui.component.ClickableIcon
import com.github.whitescent.mastify.ui.component.HeightSpacer
import com.github.whitescent.mastify.ui.component.HtmlText
import com.github.whitescent.mastify.ui.component.LocalizedClickableText
import com.github.whitescent.mastify.ui.component.SensitiveBar
import com.github.whitescent.mastify.ui.component.WidthSpacer
import com.github.whitescent.mastify.ui.component.status.action.BookmarkButton
import com.github.whitescent.mastify.ui.component.status.action.FavoriteButton
import com.github.whitescent.mastify.ui.component.status.action.ReblogButton
import com.github.whitescent.mastify.ui.component.status.action.ShareButton
import com.github.whitescent.mastify.ui.component.status.poll.StatusPoll
import com.github.whitescent.mastify.ui.theme.AppTheme
import com.github.whitescent.mastify.utils.FormatFactory
import com.github.whitescent.mastify.utils.StatusAction
import com.github.whitescent.mastify.utils.statusLinkHandler
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusDetailCard(
  status: StatusUiData,
  modifier: Modifier = Modifier,
  inReply: Boolean = false,
  action: (StatusAction) -> Unit,
  navigateToProfile: (Account) -> Unit,
  navigateToMedia: (ImmutableList<Attachment>, Int) -> Unit
) {
  var openMenuSheet by remember { mutableStateOf(false) }
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  var hideSensitiveContent by rememberSaveable(status.sensitive, status.spoilerText) {
    mutableStateOf(status.sensitive && status.spoilerText.isNotEmpty())
  }
  val displayAttachments by remember(status.attachments) {
    mutableStateOf(status.attachments.filter { it.type != "unknown" }.toImmutableList())
  }

  val context = LocalContext.current
  val primaryColor = AppTheme.colors.primaryContent
  val avatarSizePx = with(LocalDensity.current) { statusAvatarSize.toPx() }
  val contentPaddingPx = with(LocalDensity.current) { statusContentHorizontalPadding.toPx() }
  val avatarHalfSize = avatarSizePx / 2
  val avatarCenterX = avatarHalfSize + contentPaddingPx
  val replyLineColor = AppTheme.colors.replyLine

  Surface(
    modifier = modifier.fillMaxWidth(),
    color = AppTheme.colors.background
  ) {
    Column(
      modifier = Modifier
        .let {
          if (inReply) {
            it.drawWithContent {
              val (startOffsetY, endOffsetY) = 0f to avatarHalfSize
              drawLine(
                color = replyLineColor,
                start = Offset(avatarCenterX, startOffsetY),
                end = Offset(avatarCenterX, endOffsetY),
                cap = StrokeCap.Round,
                strokeWidth = 4f
              )
              drawContent()
            }
          } else it
        }
        .padding(statusContentHorizontalPadding, statusContentVerticalPadding)
    ) {
      CenterRow(modifier = Modifier.fillMaxWidth()) {
        CircleShapeAsyncImage(
          model = status.avatar,
          modifier = Modifier.size(statusAvatarSize),
          shape = AppTheme.shape.smallAvatar,
          onClick = { navigateToProfile(status.actionable.account) }
        )
        WidthSpacer(value = 7.dp)
        Column(modifier = Modifier.weight(1f)) {
          HtmlText(
            text = status.displayName,
            fontSize = 17.sp,
            fontWeight = FontWeight(650),
            overflow = TextOverflow.Ellipsis,
            color = AppTheme.colors.primaryContent,
            maxLines = 1
          )
          Text(
            text = status.fullname,
            fontSize = 14.sp,
            color = AppTheme.colors.primaryContent.copy(alpha = 0.48f),
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
          )
        }
        WidthSpacer(value = 4.dp)
        ClickableIcon(
          painter = painterResource(id = R.drawable.more),
          tint = AppTheme.colors.cardMenu,
          interactiveSize = 18.dp,
          onClick = { openMenuSheet = true },
        )
      }
      Crossfade(hideSensitiveContent) { hide ->
        when (hide) {
          true -> {
            Column {
              HeightSpacer(value = 4.dp)
              SensitiveBar(spoilerText = status.spoilerText) { hideSensitiveContent = false }
            }
          }
          else -> {
            Column {
              if (status.isInReplyToSomeone) {
                LocalizedClickableText(
                  stringRes = R.string.replying_to_title,
                  highlightText = "@${status.mentions.first().username}",
                  style = TextStyle(color = AppTheme.colors.primaryContent.copy(0.6f)),
                  onClick = {
                    statusLinkHandler(
                      mentions = status.mentions,
                      context = context,
                      primaryColor = primaryColor,
                      navigateToProfile = navigateToProfile,
                      link = status.mentions.first().url
                    )
                  },
                  fontSize = 16.sp,
                  modifier = Modifier.padding(top = 6.dp)
                )
              }
              Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(top = 8.dp)
              ) {
                if (status.content.isNotEmpty()) {
                  SelectionContainer {
                    HtmlText(
                      text = status.content,
                      fontSize = 18.sp,
                      color = AppTheme.colors.primaryContent,
                      onLinkClick = { span ->
                        statusLinkHandler(
                          mentions = status.mentions,
                          context = context,
                          primaryColor = primaryColor,
                          navigateToProfile = navigateToProfile,
                          link = span
                        )
                      },
                      overflow = TextOverflow.Ellipsis,
                      filterMentionText = status.isInReplyToSomeone
                    )
                  }
                }
                StatusLinkPreviewCard(card = status.card)
                StatusPoll(status.poll) { id, choices ->
                  action(StatusAction.VotePoll(id, choices, status.actionable))
                }
                if (displayAttachments.isNotEmpty()) {
                  StatusMedia(
                    attachments = displayAttachments,
                    onClick = { targetIndex ->
                      navigateToMedia(displayAttachments, targetIndex)
                    },
                  )
                }
              }
            }
          }
        }
      }
      HeightSpacer(value = 8.dp)
      StatusDetailInfo(
        reblogsCount = status.reblogsCount,
        favouritesCount = status.favouritesCount,
        createdAt = status.createdAt,
        application = status.application
      )
      HeightSpacer(value = 8.dp)
      StatusDetailActionsRow(
        statusUiData = status,
        action = action
      )
    }
  }
  if (openMenuSheet) {
    StatusActionDrawer(
      sheetState = sheetState,
      statusUiData = status,
      actionHandler = action,
      onDismissRequest = { openMenuSheet = false }
    )
  }
}

@Composable
private fun StatusDetailActionsRow(
  statusUiData: StatusUiData,
  action: (StatusAction) -> Unit,
  modifier: Modifier = Modifier
) {
  CenterRow(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(20.dp)
  ) {
    FavoriteButton(
      favorited = statusUiData.favorited,
      modifier = Modifier.size(statusDetailActionsIconSize),
      unfavoritedColor = AppTheme.colors.primaryContent
    ) {
      action(StatusAction.Favorite(statusUiData.actionableId, it))
    }
    ReblogButton(
      reblogged = statusUiData.reblogged,
      modifier = Modifier.size(statusDetailActionsIconSize),
      unreblogColor = AppTheme.colors.primaryContent,
      enabled = statusUiData.visibility.rebloggingAllowed
    ) {
      action(StatusAction.Reblog(statusUiData.actionableId, it))
    }
    BookmarkButton(
      bookmarked = statusUiData.bookmarked,
      modifier = Modifier.size(statusDetailActionsIconSize),
      onClick = {
        action(StatusAction.Bookmark(statusUiData.actionableId, it))
      }
    )
    ShareButton(
      link = statusUiData.link,
      id = R.drawable.share_network,
      tint = AppTheme.colors.primaryContent,
      modifier = Modifier.size(statusDetailActionsIconSize)
    )
  }
}

@Composable
private fun StatusDetailInfo(
  reblogsCount: Int,
  favouritesCount: Int,
  createdAt: String,
  application: Application?,
) {
  Column {
    CenterRow(Modifier.fillMaxWidth()) {
      CenterRow(Modifier.weight(1f)) {
        Icon(
          painter = painterResource(id = R.drawable.globe),
          contentDescription = null,
          tint = Color(0xFF999999),
          modifier = Modifier.size(20.dp)
        )
        WidthSpacer(value = 4.dp)
        Text(
          text = "${FormatFactory.getLocalizedDateTime(createdAt)} ${FormatFactory.getTime(createdAt)}",
          color = Color(0xFF999999),
          fontSize = 14.sp,
        )
      }
      application?.let {
        if (it.name.isNotEmpty()) {
          Text(
            text = application.name,
            color = AppTheme.colors.hintText
          )
        }
      }
    }
    Crossfade(targetState = favouritesCount != 0 || reblogsCount != 0) {
      if (it) {
        Column {
          HeightSpacer(value = 8.dp)
          CenterRow {
            Crossfade(favouritesCount != 0) { showFavCount ->
              CenterRow {
                if (showFavCount) {
                  AnimatedText(
                    text = pluralStringResource(id = R.plurals.favs, favouritesCount, favouritesCount),
                    color = Color(0xFFF91880),
                  )
                  WidthSpacer(value = 8.dp)
                }
              }
            }
            Crossfade(reblogsCount != 0) { showReblogCount ->
              if (showReblogCount) {
                AnimatedText(
                  text = pluralStringResource(id = R.plurals.reblogs, reblogsCount, reblogsCount),
                  color = Color(0xFF00BA7C)
                )
              }
            }
          }
        }
      }
    }
  }
}

private val statusDetailActionsIconSize = 24.dp
