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

import android.net.Uri
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.whitescent.R
import com.github.whitescent.mastify.data.model.ui.StatusUiData
import com.github.whitescent.mastify.data.model.ui.StatusUiData.ReplyChainType
import com.github.whitescent.mastify.data.model.ui.StatusUiData.ReplyChainType.Continue
import com.github.whitescent.mastify.data.model.ui.StatusUiData.ReplyChainType.End
import com.github.whitescent.mastify.data.model.ui.StatusUiData.ReplyChainType.Start
import com.github.whitescent.mastify.network.model.account.Account
import com.github.whitescent.mastify.network.model.status.Status.Attachment
import com.github.whitescent.mastify.ui.component.AnimatedText
import com.github.whitescent.mastify.ui.component.CenterRow
import com.github.whitescent.mastify.ui.component.CircleShapeAsyncImage
import com.github.whitescent.mastify.ui.component.ClickableIcon
import com.github.whitescent.mastify.ui.component.HeightSpacer
import com.github.whitescent.mastify.ui.component.HtmlText
import com.github.whitescent.mastify.ui.component.SensitiveBar
import com.github.whitescent.mastify.ui.component.WidthSpacer
import com.github.whitescent.mastify.ui.component.status.action.FavoriteButton
import com.github.whitescent.mastify.ui.component.status.action.ReblogButton
import com.github.whitescent.mastify.ui.component.status.action.ShareButton
import com.github.whitescent.mastify.ui.component.status.poll.StatusPoll
import com.github.whitescent.mastify.ui.theme.AppTheme
import com.github.whitescent.mastify.utils.StatusAction
import com.github.whitescent.mastify.utils.getRelativeTimeSpanString
import com.github.whitescent.mastify.utils.launchCustomChromeTab
import com.microsoft.fluentui.tokenized.drawer.rememberBottomDrawerState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.toInstant

@Composable
fun StatusListItem(
  status: StatusUiData,
  replyChainType: ReplyChainType,
  hasUnloadedParent: Boolean,
  modifier: Modifier = Modifier,
  action: (StatusAction) -> Unit,
  navigateToDetail: () -> Unit,
  navigateToProfile: (Account) -> Unit,
  navigateToMedia: (ImmutableList<Attachment>, Int) -> Unit
) {
  val avatarSizePx = with(LocalDensity.current) { statusAvatarSize.toPx() }
  val contentPaddingPx = with(LocalDensity.current) { statusContentHorizontalPadding.toPx() }
  val avatarHalfSize = avatarSizePx / 2
  val avatarCenterX = avatarHalfSize + contentPaddingPx
  val replyLineColor = AppTheme.colors.replyLine
  Surface(
    modifier = modifier,
    color = AppTheme.colors.background
  ) {
    Column {
      if (hasUnloadedParent && (status.reblog == null)) {
        CenterRow(
          modifier = Modifier.padding(top = statusContentVerticalPadding)
        ) {
          Box(
            modifier = Modifier
              .padding(horizontal = statusContentHorizontalPadding)
              .size(statusAvatarSize),
            contentAlignment = Alignment.Center
          ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
              repeat(3) {
                Box(Modifier.size(3.dp).background(replyLineColor, CircleShape))
              }
            }
          }
          Text(
            text = stringResource(id = R.string.started_a_discussion_thread),
            fontSize = 14.sp,
            fontWeight = FontWeight(600),
            color = AppTheme.colors.hintText,
          )
        }
      }
      Column(
        modifier = Modifier
          .clickable(
            onClick = navigateToDetail,
            indication = null,
            interactionSource = remember { MutableInteractionSource() }
          )
          .let {
            if (status.reblog == null) {
              it.drawWithContent {
                val itemHeight = this.size.height
                val (startOffsetY, endOffsetY) = when (replyChainType) {
                  Start -> {
                    if (!hasUnloadedParent) avatarHalfSize to itemHeight
                    else 0f to itemHeight
                  }
                  Continue -> 0f to itemHeight
                  End -> 0f to avatarHalfSize
                  else -> 0f to 0f
                }
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
      ) {
        status.reblog?.let {
          StatusSource(
            reblogAvatar = status.rebloggedAvatar,
            reblogDisplayName = status.reblogDisplayName
          ) { navigateToProfile(status.account) }
        }
        StatusContent(
          statusUiData = status,
          action = action,
          onClickMedia = {
            navigateToMedia(status.attachments, it)
          },
          navigateToProfile = navigateToProfile
        )
      }
    }
  }
}

@Composable
private fun StatusSource(
  reblogAvatar: String,
  reblogDisplayName: String,
  navigateToProfile: () -> Unit
) {
  CenterRow(
    Modifier
      .fillMaxWidth()
      .padding(start = 24.dp, top = 8.dp),
  ) {
    CircleShapeAsyncImage(
      model = reblogAvatar,
      modifier = Modifier.size(24.dp),
      shape = AppTheme.shape.smallAvatar.copy(CornerSize(8.dp)),
      onClick = { navigateToProfile() }
    )
    WidthSpacer(value = 4.dp)
    HtmlText(
      text = stringResource(id = R.string.user_boosted, reblogDisplayName),
      style = TextStyle(
        color = AppTheme.colors.cardCaption,
        fontSize = AppTheme.typography.statusRepost.fontSize,
      )
    )
    WidthSpacer(value = 4.dp)
    Image(
      painter = painterResource(id = R.drawable.reblog),
      contentDescription = null,
      modifier = Modifier.size(18.dp),
    )
  }
}

@Composable
private fun StatusContent(
  statusUiData: StatusUiData,
  modifier: Modifier = Modifier,
  action: (StatusAction) -> Unit,
  onClickMedia: (Int) -> Unit,
  navigateToProfile: (Account) -> Unit,
) {
  val scope = rememberCoroutineScope()
  val context = LocalContext.current
  val primaryColor = AppTheme.colors.primaryContent
  var hideSensitiveContent by rememberSaveable(statusUiData.sensitive, statusUiData.spoilerText) {
    mutableStateOf(statusUiData.sensitive || (statusUiData.spoilerText.isNotEmpty()))
  }
  val displayAttachments by remember(statusUiData.attachments) {
    mutableStateOf(statusUiData.attachments.filter { it.type != "unknown" })
  }
  val drawerState = rememberBottomDrawerState()

  Box(modifier = modifier) {
    Row(
      modifier = Modifier.padding(statusContentHorizontalPadding, statusContentVerticalPadding)
    ) {
      CircleShapeAsyncImage(
        model = statusUiData.avatar,
        modifier = Modifier.size(statusAvatarSize),
        shape = AppTheme.shape.smallAvatar.copy(CornerSize(12.dp)),
        onClick = { navigateToProfile(statusUiData.actionable.account) }
      )
      WidthSpacer(value = 7.dp)
      Column(modifier = Modifier.align(Alignment.Top)) {
        CenterRow {
          Column(modifier = Modifier.weight(1f)) {
            HtmlText(
              text = statusUiData.displayName,
              style = AppTheme.typography.statusDisplayName,
              overflow = TextOverflow.Ellipsis,
              maxLines = 1
            )
            Text(
              text = statusUiData.fullname,
              style = AppTheme.typography.statusUsername.copy(
                color = AppTheme.colors.primaryContent.copy(alpha = 0.48f),
              ),
              overflow = TextOverflow.Ellipsis,
              maxLines = 1,
            )
          }
          WidthSpacer(value = 4.dp)
          Column {
            CenterRow {
              Text(
                text = remember(statusUiData.createdAt) {
                  getRelativeTimeSpanString(
                    context,
                    statusUiData.createdAt.toInstant().toEpochMilliseconds(),
                    Clock.System.now().toEpochMilliseconds()
                  )
                },
                style = AppTheme.typography.statusUsername.copy(
                  color = AppTheme.colors.primaryContent.copy(alpha = 0.48f),
                ),
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
              )
              when (statusUiData.visibility) {
                StatusUiData.Visibility.Private -> {
                  Icon(
                    painter = painterResource(id = R.drawable.lock),
                    contentDescription = null,
                    modifier = Modifier
                      .padding(horizontal = 8.dp)
                      .size(20.dp),
                    tint = AppTheme.colors.cardMenu,
                  )
                }
                StatusUiData.Visibility.Unlisted -> {
                  Icon(
                    painter = painterResource(id = R.drawable.lock_open),
                    contentDescription = null,
                    modifier = Modifier
                      .padding(horizontal = 8.dp)
                      .size(20.dp),
                    tint = AppTheme.colors.cardMenu,
                  )
                }
                else -> WidthSpacer(value = 4.dp)
              }
              ClickableIcon(
                painter = painterResource(id = R.drawable.more),
                tint = AppTheme.colors.cardMenu,
                interactiveSize = 18.dp,
                onClick = { scope.launch { drawerState.open() } }
              )
            }
          }
        }
        Crossfade(hideSensitiveContent) {
          when (it) {
            true -> {
              Column {
                HeightSpacer(value = 4.dp)
                SensitiveBar(
                  spoilerText = statusUiData.spoilerText.ifEmpty {
                    statusUiData.parsedContent.ifEmpty {
                      stringResource(id = R.string.sensitive_content)
                    }
                  },
                  onClick = {
                    hideSensitiveContent = false
                  }
                )
              }
            }
            else -> {
              Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(top = 8.dp)
              ) {
                if (statusUiData.content.isNotEmpty()) {
                  HtmlText(
                    text = statusUiData.content,
                    fontSize = (15.5).sp,
                    maxLines = 11,
                    onLinkClick = { span ->
                      val mention = statusUiData.mentions.firstOrNull { it.url == span }
                      if (mention != null) {
                        navigateToProfile(mention.toAccount())
                      } else {
                        launchCustomChromeTab(
                          context = context,
                          uri = Uri.parse(span),
                          toolbarColor = primaryColor.toArgb(),
                        )
                      }
                    },
                    overflow = TextOverflow.Ellipsis
                  )
                }
                StatusLinkPreviewCard(card = statusUiData.card)
                StatusPoll(statusUiData.poll) { id, choices ->
                  action(StatusAction.VotePoll(id, choices, statusUiData.actionable))
                }
                if (displayAttachments.isNotEmpty()) {
                  StatusMedia(
                    attachments = displayAttachments.toImmutableList(),
                    onClick = onClickMedia,
                  )
                }
              }
            }
          }
        }
        HeightSpacer(value = 6.dp)
        StatusActionsRow(
          statusId = statusUiData.actionableId,
          repliesCount = statusUiData.repliesCount,
          reblogsCount = statusUiData.reblogsCount,
          favoritesCount = statusUiData.favouritesCount,
          favorited = statusUiData.favorited,
          reblogged = statusUiData.reblogged,
          rebloggingAllowed = statusUiData.visibility.rebloggingAllowed,
          link = statusUiData.link,
          action = action
        )
      }
    }
  }
  StatusActionDrawer(
    drawerState = drawerState,
    statusUiData = statusUiData,
    actionHandler = action
  )
}

@Composable
private fun StatusActionsRow(
  statusId: String,
  repliesCount: Int,
  reblogsCount: Int,
  favoritesCount: Int,
  favorited: Boolean,
  reblogged: Boolean,
  rebloggingAllowed: Boolean,
  link: String,
  action: (StatusAction) -> Unit,
  modifier: Modifier = Modifier
) {
  CenterRow(modifier = modifier) {
    CenterRow(
      modifier = Modifier.weight(1f),
      horizontalArrangement = Arrangement.spacedBy(22.dp)
    ) {
      CenterRow {
        ClickableIcon(
          painter = painterResource(id = R.drawable.comment),
          modifier = Modifier.size(statusActionsIconSize),
          tint = AppTheme.colors.cardAction,
        )
        if (repliesCount != 0) {
          WidthSpacer(value = 2.dp)
          Text(
            text = repliesCount.toString(),
            style = AppTheme.typography.statusActions,
          )
        }
      }
      CenterRow {
        FavoriteButton(
          favorited = favorited,
          modifier = Modifier.size(statusActionsIconSize)
        ) {
          action(StatusAction.Favorite(statusId, it))
        }
        if (favoritesCount != 0) WidthSpacer(value = 2.dp)
        AnimatedText(
          text = if (favoritesCount != 0) favoritesCount.toString() else "",
          style = AppTheme.typography.statusActions,
        )
      }
      CenterRow {
        ReblogButton(
          reblogged = reblogged,
          enabled = rebloggingAllowed,
          modifier = Modifier.size(statusActionsIconSize)
        ) {
          action(StatusAction.Reblog(statusId, it))
        }
        WidthSpacer(value = 2.dp)
        AnimatedText(
          text = if (reblogsCount != 0) reblogsCount.toString() else "",
          style = TextStyle(color = AppTheme.colors.cardAction),
        )
      }
    }
    CenterRow {
      Surface(
        modifier = Modifier
          .size(height = 16.dp, width = 1.dp)
          .clip(RoundedCornerShape(100.dp)),
        color = AppTheme.colors.cardAction.copy(alpha = 0.12f),
      ) { }
      WidthSpacer(value = 16.dp)
      ShareButton(
        link = link,
        modifier = Modifier.size(statusActionsIconSize)
      )
    }
  }
}

val statusContentHorizontalPadding = 12.dp
val statusContentVerticalPadding = 10.dp
val statusAvatarSize = 40.dp
private val statusActionsIconSize = 20.dp
