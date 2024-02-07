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

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.window.layout.WindowMetricsCalculator
import com.github.whitescent.R
import com.github.whitescent.mastify.network.model.account.Account
import com.github.whitescent.mastify.ui.theme.AppTheme
import com.github.whitescent.mastify.utils.PostState
import com.github.whitescent.mastify.utils.windowBottomEndCornerRadius
import com.github.whitescent.mastify.utils.windowBottomStartCornerRadius

@Composable
fun ReplyTextField(
  targetAccount: List<Account>,
  fieldValue: TextFieldValue,
  postState: PostState,
  modifier: Modifier = Modifier,
  showReplyUserButton: Boolean = false,
  onValueChange: (TextFieldValue) -> Unit,
  replyToStatus: () -> Unit,
  openEmojiPicker: () -> Unit,
  openReplyUserDialog: () -> Unit,
) {
  var expand by remember { mutableStateOf(false) }
  val focusRequester = remember { FocusRequester() }
  val currentWindowSize = WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(
    LocalContext.current,
  )
  var bottomStartRadius by remember { mutableStateOf(0.dp) }
  var bottomEndRadius by remember { mutableStateOf(0.dp) }
  val bottomStartCornerRadius = windowBottomStartCornerRadius()
  val bottomEndCornerRadius = windowBottomEndCornerRadius()

  Surface(
    modifier = modifier
      .imePadding()
      .onGloballyPositioned {
        // If the text field is at the bottom of the window, make the bottom corners rounded
        if (it.boundsInWindow().bottom == currentWindowSize.bounds.bottom.toFloat()) {
          bottomStartRadius = bottomStartCornerRadius
          bottomEndRadius = bottomEndCornerRadius
        } else {
          bottomStartRadius = 0.dp
          bottomEndRadius = 0.dp
        }
      },
    color = AppTheme.colors.background,
    shape = RoundedCornerShape(
      topStart = 15.dp,
      topEnd = 15.dp,
      bottomStart = bottomStartRadius,
      bottomEnd = bottomEndRadius,
    ),
    border = BorderStroke(1.dp, AppTheme.colors.replyTextFieldBorder),
  ) {
    AnimatedContent(
      targetState = expand
    ) {
      when (it) {
        true -> {
          ReplyTextFieldWithToolBar(
            fieldValue = fieldValue,
            focusRequester = focusRequester,
            targetAccount = targetAccount,
            postState = postState,
            onValueChange = onValueChange,
            showReplyUserButton = showReplyUserButton,
            onFocusChanged = { focused -> expand = focused },
            replyToStatus = replyToStatus,
            openEmojiPicker = openEmojiPicker,
            openReplyUserDialog = openReplyUserDialog
          )
        }
        else -> {
          Column(Modifier.navigationBarsPadding().padding(20.dp).heightIn(max = 300.dp)) {
            if (fieldValue.text.isNotEmpty()) {
              ReplyTitleBar(targetAccount)
              HeightSpacer(value = 8.dp)
            }
            Text(
              text = fieldValue.text.ifEmpty { stringResource(id = R.string.reply_placeholder) },
              color = when (fieldValue.text.isEmpty()) {
                true -> Color(0xFFB6B6B6)
                else -> AppTheme.colors.primaryContent
              },
              modifier = Modifier
                .fillMaxWidth()
                .clickable(
                  onClick = {
                    expand = true
                  },
                  indication = null,
                  interactionSource = remember { MutableInteractionSource() }
                ),
            )
          }
        }
      }
    }
  }
}

@Composable
private fun ReplyTitleBar(
  targetAccount: List<Account>,
) {
  when (targetAccount.size) {
    1 -> {
      CenterRow {
        Icon(
          painter = painterResource(id = R.drawable.reply_border_0_5),
          contentDescription = null,
          tint = AppTheme.colors.primaryContent,
          modifier = Modifier.size(20.dp)
        )
        CircleShapeAsyncImage(
          model = targetAccount.first().avatar,
          modifier = Modifier.padding(horizontal = 12.dp).size(32.dp),
          shape = AppTheme.shape.smallAvatar
        )
        TextWithEmoji(
          text = targetAccount.first().realDisplayName,
          emojis = targetAccount.first().emojis,
          fontSize = 18.sp,
          fontWeight = FontWeight.Medium,
          color = AppTheme.colors.primaryContent,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
    }
    else -> {
      CenterRow {
        Icon(
          painter = painterResource(id = R.drawable.reply_border_0_5),
          contentDescription = null,
          tint = AppTheme.colors.primaryContent,
          modifier = Modifier.size(20.dp)
        )
        CenterRow(horizontalArrangement = Arrangement.spacedBy((-32).dp)) {
          targetAccount.forEachIndexed { _, account ->
            CircleShapeAsyncImage(
              model = account.avatar,
              modifier = Modifier.padding(horizontal = 12.dp).size(32.dp),
              shape = AppTheme.shape.smallAvatar
            )
          }
        }
        TextWithEmoji(
          text = when (targetAccount.size) {
            2 -> stringResource(
              id = R.string.reply_two_user,
              targetAccount.first().realDisplayName,
              targetAccount.last().realDisplayName
            )
            3 -> stringResource(
              id = R.string.reply_three_user,
              targetAccount.first().realDisplayName,
              targetAccount[1].realDisplayName,
              targetAccount.last().realDisplayName
            )
            else -> stringResource(
              id = R.string.reply_more_user,
              targetAccount.first().realDisplayName,
              targetAccount.last().realDisplayName,
              targetAccount.size - 2
            )
          },
          emojis = targetAccount.flatMap { it.emojis },
          fontSize = 16.sp,
          fontWeight = FontWeight.Medium,
          color = AppTheme.colors.primaryContent,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
    }
  }
}

@Composable
private fun ReplyTextFieldWithToolBar(
  fieldValue: TextFieldValue,
  focusRequester: FocusRequester,
  targetAccount: List<Account>,
  postState: PostState,
  showReplyUserButton: Boolean,
  onValueChange: (TextFieldValue) -> Unit,
  onFocusChanged: (Boolean) -> Unit,
  replyToStatus: () -> Unit,
  openReplyUserDialog: () -> Unit,
  openEmojiPicker: () -> Unit,
) {
  var isFocused by remember { mutableStateOf(false) }
  val focusManager = LocalFocusManager.current
  val keyboard = LocalSoftwareKeyboardController.current

  Column(
    modifier = Modifier.navigationBarsPadding().padding(horizontal = 12.dp)
  ) {
    Box(
      Modifier
        .padding(vertical = 12.dp)
        .width(64.dp)
        .height(5.dp)
        .background(Color(0xFFD9D9D9), RoundedCornerShape(3.dp))
        .align(Alignment.CenterHorizontally)
    )
    ReplyTitleBar(targetAccount)
    BasicTextField(
      value = fieldValue,
      onValueChange = onValueChange,
      modifier = Modifier
        .fillMaxWidth()
        .animateContentSize()
        .padding(vertical = 16.dp)
        .heightIn(max = 140.dp)
        .focusRequester(focusRequester)
        .onFocusChanged {
          onFocusChanged(it.isFocused)
          isFocused = it.isFocused
        },
      textStyle = TextStyle(fontSize = 16.sp, color = AppTheme.colors.primaryContent),
      cursorBrush = SolidColor(AppTheme.colors.primaryContent)
    ) {
      Box {
        if (fieldValue.text.isEmpty()) {
          Text(
            text = stringResource(id = R.string.reply_placeholder),
            color = Color(0xFFB6B6B6)
          )
        }
        it()
      }
    }
    CenterRow(Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
      ClickableIcon(
        painter = painterResource(id = R.drawable.image),
        tint = AppTheme.colors.cardAction,
        modifier = Modifier.size(24.dp),
        onClick = { /*TODO*/ },
      )
      WidthSpacer(value = 8.dp)
      ClickableIcon(
        painter = painterResource(id = R.drawable.emoji),
        tint = AppTheme.colors.cardAction,
        interactiveSize = 24.dp,
        onClick = openEmojiPicker,
      )
      if (showReplyUserButton) {
        WidthSpacer(value = 8.dp)
        ClickableIcon(
          painter = painterResource(id = R.drawable.user_gear),
          tint = AppTheme.colors.cardAction,
          interactiveSize = 24.dp,
          onClick = openReplyUserDialog,
        )
      }
      Spacer(Modifier.weight(1f))
      IconButton(
        onClick = {
          replyToStatus()
          keyboard?.hide()
        },
        enabled = fieldValue.text.isNotEmpty(),
        colors = IconButtonDefaults.filledIconButtonColors(
          containerColor = when (postState !is PostState.Failure) {
            true -> AppTheme.colors.accent
            else -> Color(0xFFF53232)
          },
          contentColor = Color.White,
          disabledContentColor = Color.Gray
        ),
      ) {
        when (postState) {
          is PostState.Idle, PostState.Success, is PostState.Failure -> {
            Icon(
              painter = painterResource(id = R.drawable.send),
              contentDescription = null,
              modifier = Modifier.size(24.dp)
            )
          }
          is PostState.Posting -> {
            CircularProgressIndicator(color = Color.White, strokeWidth = 4.dp)
          }
        }
      }
    }
  }
  LaunchedEffect(Unit) {
    focusRequester.requestFocus()
  }
  LaunchedEffect(postState) {
    if (postState is PostState.Success) focusManager.clearFocus()
  }
  BackHandler(isFocused) {
    focusManager.clearFocus()
  }
}
