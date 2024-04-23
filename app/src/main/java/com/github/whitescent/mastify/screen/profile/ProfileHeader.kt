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

package com.github.whitescent.mastify.screen.profile

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.bundleOf
import coil.compose.AsyncImage
import com.github.whitescent.R
import com.github.whitescent.mastify.ui.component.AvatarWithCover
import com.github.whitescent.mastify.ui.component.CenterRow
import com.github.whitescent.mastify.ui.component.CircleShapeAsyncImage
import com.github.whitescent.mastify.ui.component.HeightSpacer
import com.github.whitescent.mastify.ui.component.HtmlText
import com.github.whitescent.mastify.ui.component.TextWithEmoji
import com.github.whitescent.mastify.ui.component.WidthSpacer
import com.github.whitescent.mastify.ui.component.avatarStartPadding
import com.github.whitescent.mastify.ui.component.buildPlainText
import com.github.whitescent.mastify.ui.component.button.EditProfileButton
import com.github.whitescent.mastify.ui.component.button.FollowButton
import com.github.whitescent.mastify.ui.component.button.SubscribeButton
import com.github.whitescent.mastify.ui.component.dialog.BasicDialog
import com.github.whitescent.mastify.ui.component.dialog.rememberDialogState
import com.github.whitescent.mastify.ui.component.profileCollapsingLayout.ProfileLayoutState
import com.github.whitescent.mastify.ui.theme.AppTheme
import com.github.whitescent.mastify.ui.theme.shape.SmoothCornerShape
import com.github.whitescent.mastify.utils.FormatFactory
import com.github.whitescent.mastify.utils.FormatFactory.getAcctFromUrl
import com.github.whitescent.mastify.utils.PostState
import com.github.whitescent.mastify.utils.clickableWithoutIndication
import com.github.whitescent.mastify.utils.launchCustomChromeTab
import com.github.whitescent.mastify.utils.statusLinkHandler
import com.github.whitescent.mastify.viewModel.ProfileUiState
import kotlinx.coroutines.launch
import java.net.MalformedURLException

data class FieldChipColorScheme(
  val containerColor: Color,
  val contentColor: Color
) {
  companion object {
    val saver: Saver<FieldChipColorScheme, *> = Saver(
      save = {
        bundleOf(
          "containerColor" to it.containerColor.toArgb(),
          "contentColor" to it.contentColor.toArgb()
        )
      },
      restore = {
        FieldChipColorScheme(
          containerColor = Color(it.getInt("containerColor")),
          contentColor = Color(it.getInt("contentColor"))
        )
      }
    )
  }
}

@Composable
fun ProfileHeader(
  uiState: ProfileUiState,
  profileLayoutState: ProfileLayoutState,
  searchAccount: (String) -> Unit,
  follow: (Boolean) -> Unit,
  subscribe: (Boolean) -> Unit,
  navigateToAccount: () -> Unit,
  navigateToTagInfo: (String) -> Unit
) {
  val dialogState = rememberDialogState()
  val scope = rememberCoroutineScope()
  val context = LocalContext.current
  val clipManager = LocalClipboardManager.current
  val fieldChipColorScheme = listOf(
    FieldChipColorScheme(Color(0XFFfffbea), Color(0xFFb3530d)),
    FieldChipColorScheme(Color(0XFFF0F9FF), Color(0xFF046B9F))
  )
  val metrics = listOf(
    uiState.account.statusesCount,
    uiState.account.followingCount,
    uiState.account.followersCount
  )
  Column {
    AvatarWithCover(
      cover = {
        if (uiState.account.isEmptyHeader) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(200.dp)
              .background(AppTheme.colors.defaultHeader),
          )
        } else {
          AsyncImage(
            model = uiState.account.header,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth().height(200.dp),
          )
        }
      },
      avatar = {
        CircleShapeAsyncImage(
          model = uiState.account.avatar,
          modifier = Modifier
            .graphicsLayer {
              scaleY = (1 - profileLayoutState.progress).coerceAtLeast(0.7f)
              scaleX = (1 - profileLayoutState.progress).coerceAtLeast(0.7f)
            }
            .shadow(12.dp, AppTheme.shape.largeAvatar)
            .size(80.dp),
          shape = AppTheme.shape.largeAvatar
        )
      },
      actions = {
        CenterRow(
          modifier = Modifier.padding(12.dp)
        ) {
          Text(
            text = stringResource(
              id = R.string.account_joined,
              FormatFactory.getLocalizedDateTime(uiState.account.createdAt)
            ),
            color = AppTheme.colors.primaryContent.copy(0.4f),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
          )
          WidthSpacer(value = 6.dp)
          Icon(
            painter = painterResource(R.drawable.shooting_star),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = AppTheme.colors.primaryContent
          )
        }
      },
    )
    HeightSpacer(value = 10.dp)
    Column(
      modifier = Modifier.padding(horizontal = avatarStartPadding),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Column {
        TextWithEmoji(
          text = uiState.account.realDisplayName,
          emojis = uiState.account.emojis,
          fontSize = 22.sp,
          fontWeight = FontWeight(650),
          color = AppTheme.colors.primaryContent
        )
        HeightSpacer(value = 2.dp)
        Text(
          text = uiState.account.fullname,
          style = AppTheme.typography.statusUsername.copy(
            color = AppTheme.colors.primaryContent.copy(alpha = 0.48f),
          ),
          overflow = TextOverflow.Ellipsis,
          maxLines = 1,
          fontSize = 16.sp,
        )
      }
      if (uiState.account.note.isNotEmpty()) {
        HtmlText(
          text = uiState.account.noteWithEmoji,
          style = TextStyle(
            fontSize = 16.sp,
            color = AppTheme.colors.primaryContent.copy(.85f)
          ),
          onLinkClick = { url ->
            if (url.contains("@")) {
              try {
                searchAccount(getAcctFromUrl(url))
              } catch (e: MalformedURLException) {
                Toast.makeText(context, "host Invalid", Toast.LENGTH_SHORT).show()
              }
            } else {
              statusLinkHandler(
                mentions = emptyList(),
                context = context,
                link = url,
                navigateToProfile = {},
                navigateToTagInfo = { navigateToTagInfo(it) }
              )
            }
          }
        )
      }
      Column(
        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        if (uiState.account.fieldsWithEmoji.isNotEmpty()) {
          FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            uiState.account.fieldsWithEmoji.forEach {
              val parsedFieldValue = buildAnnotatedString {
                append(buildPlainText(it.value, false))
              }.text
              val tooltipState = rememberTooltipState(isPersistent = true)
              val colorScheme = rememberSaveable(saver = FieldChipColorScheme.saver) {
                fieldChipColorScheme.random()
              }
              TooltipBox(
                positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(14.dp),
                tooltip = {
                  Box(
                    modifier = Modifier
                      .wrapContentWidth()
                      .padding(horizontal = 14.dp)
                      .border(1.dp, colorScheme.contentColor.copy(.4f), CircleShape)
                      .clip(SmoothCornerShape(7.dp))
                      .background(colorScheme.containerColor, CircleShape)
                  ) {
                    CenterRow(
                      modifier = Modifier
                        .let {
                          when (FormatFactory.isValidUrl(parsedFieldValue)) {
                            true -> it.clickableWithoutIndication {
                              launchCustomChromeTab(
                                context = context,
                                uri = Uri.parse(FormatFactory.ensureHttpPrefix(parsedFieldValue))
                              )
                            }
                            false -> it
                          }
                        }
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                      HtmlText(
                        text = it.value,
                        fontSize = 16.sp,
                        fontWeight = FontWeight(450),
                        overflow = TextOverflow.Ellipsis,
                        onLinkClick = { url ->
                          launchCustomChromeTab(
                            context = context,
                            uri = Uri.parse(url)
                          )
                        },
                        color = colorScheme.contentColor,
                        modifier = Modifier.weight(1f, false)
                      )
                      WidthSpacer(value = 4.dp)
                      when (FormatFactory.isValidUrl(parsedFieldValue)) {
                        true -> {
                          Icon(
                            painter = painterResource(id = R.drawable.link_simple),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = colorScheme.contentColor
                          )
                        }
                        else -> {
                          Icon(
                            painter = painterResource(id = R.drawable.copy_fill),
                            contentDescription = null,
                            modifier = Modifier
                              .size(24.dp)
                              .clickableWithoutIndication {
                                clipManager.setText(
                                  buildAnnotatedString {
                                    append(parsedFieldValue)
                                  }
                                )
                                tooltipState.dismiss()
                              },
                            tint = colorScheme.contentColor
                          )
                        }
                      }
                    }
                  }
                },
                state = tooltipState,
              ) {
                Box(
                  modifier = Modifier
                    .clip(CircleShape)
                    .background(colorScheme.containerColor, CircleShape)
                    .border(1.dp, colorScheme.contentColor.copy(.4f), CircleShape)
                    .height(30.dp)
                    .clickable {
                      scope.launch { tooltipState.show() }
                    },
                  contentAlignment = Alignment.Center
                ) {
                  CenterRow(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                  ) {
                    it.verifiedAt?.let {
                      Icon(
                        painter = painterResource(id = R.drawable.seal_check),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = colorScheme.contentColor
                      )
                      WidthSpacer(value = 4.dp)
                    }
                    HtmlText(
                      text = it.name,
                      color = colorScheme.contentColor,
                      fontWeight = FontWeight.Medium
                    )
                  }
                }
              }
            }
          }
        }
        CenterRow(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(36.dp)
        ) {
          metrics.forEachIndexed { index, item ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(
                text = "$item",
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                color = AppTheme.colors.primaryContent
              )
              WidthSpacer(value = 4.dp)
              Text(
                text = stringResource(
                  id = when (index) {
                    0 -> R.string.post_title
                    1 -> R.string.following_title
                    else -> R.string.follower_title
                  }
                ),
                color = AppTheme.colors.primaryContent.copy(0.5f),
                fontSize = 14.sp
              )
            }
          }
        }
        if (uiState.isSelf != null) {
          CenterRow(Modifier.fillMaxWidth()) {
            when (uiState.isSelf) {
              true -> EditProfileButton(Modifier.fillMaxWidth())
              else -> uiState.relationship?.let { relationship ->
                val followed = relationship.following
                val subscribed = relationship.notifying
                FollowButton(
                  followed = followed,
                  requested = relationship.requested,
                  onClick = { follow ->
                    if (!relationship.requested) follow(follow) else follow(false)
                  },
                  modifier = Modifier.weight(1f),
                  postState = uiState.followState,
                ) {
                  Text(
                    text = stringResource(
                      id = when {
                        relationship.requested -> R.string.follow_pending_title
                        else -> {
                          if (followed) R.string.following_title else R.string.follow_title
                        }
                      }
                    ),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                  )
                }
                if (followed && !relationship.requested) {
                  SubscribeButton(
                    subscribed = subscribed,
                    postState = uiState.followState,
                    onClick = subscribe,
                    modifier = Modifier.weight(1f).padding(start = 10.dp)
                  )
                }
              }
            }
          }
        }
      }
    }
  }

  LaunchedEffect(uiState.searchState) {
    when (uiState.searchState) {
      is PostState.Posting -> dialogState.showDialog()
      is PostState.Failure -> {
        dialogState.closeDialog()
        Toast.makeText(
          context,
          uiState.searchState.throwable.localizedMessage,
          Toast.LENGTH_SHORT
        ).show()
      }
      is PostState.Success -> {
        dialogState.closeDialog()
        navigateToAccount()
      }
      is PostState.Idle -> Unit
    }
  }

  BasicDialog(
    dialogState = dialogState
  ) {
    CenterRow(Modifier.padding(24.dp)) {
      Text(
        text = stringResource(id = R.string.searching_account),
        fontSize = 18.sp,
        color = AppTheme.colors.primaryContent
      )
      WidthSpacer(value = 6.dp)
      CircularProgressIndicator(
        color = AppTheme.colors.accent,
        modifier = Modifier.size(16.dp),
        strokeWidth = 1.dp
      )
    }
  }
}
