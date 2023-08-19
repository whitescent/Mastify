package com.github.whitescent.mastify.ui.component

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.whitescent.R
import com.github.whitescent.mastify.mapper.emoji.toShortCode
import com.github.whitescent.mastify.network.model.account.Account
import com.github.whitescent.mastify.ui.theme.AppTheme
import com.github.whitescent.mastify.viewModel.PostState

@Composable
fun ReplyTextField(
  targetAccount: Account,
  fieldValue: TextFieldValue,
  postState: PostState,
  onValueChange: (TextFieldValue) -> Unit,
  replyToStatus: () -> Unit
) {
  var expand by rememberSaveable { mutableStateOf(false) }
  val focusRequester = remember { FocusRequester() }
  Surface(
    modifier = Modifier.imePadding(),
    color = AppTheme.colors.background,
    shape = RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp),
    border = BorderStroke(1.dp, AppTheme.colors.divider)
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
            onFocusChanged = { focused -> expand = focused },
            replyToStatus = replyToStatus
          )
        }
        else -> {
          Column(Modifier.navigationBarsPadding().padding(20.dp)) {
            if (fieldValue.text.isNotEmpty()) {
              CenterRow {
                Icon(
                  painter = painterResource(id = R.drawable.arrow_bend_up_right),
                  contentDescription = null,
                  tint = AppTheme.colors.primaryContent,
                  modifier = Modifier.size(20.dp)
                )
                WidthSpacer(value = 6.dp)
                CircleShapeAsyncImage(
                  model = targetAccount.avatar,
                  modifier = Modifier.size(28.dp)
                )
                WidthSpacer(value = 4.dp)
                Text(
                  text = targetAccount.realDisplayName,
                  fontSize = 16.sp,
                  fontWeight = FontWeight.Medium
                )
              }
              HeightSpacer(value = 8.dp)
            }
            Text(
              text = fieldValue.text.ifEmpty { "有什么想要分享的？" },
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
                )
            )
          }
        }
      }
    }
  }
}

@Composable
private fun ReplyTextFieldWithToolBar(
  fieldValue: TextFieldValue,
  focusRequester: FocusRequester,
  targetAccount: Account,
  postState: PostState,
  onValueChange: (TextFieldValue) -> Unit,
  onFocusChanged: (Boolean) -> Unit,
  replyToStatus: () -> Unit
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
    CenterRow {
      Icon(
        painter = painterResource(id = R.drawable.arrow_bend_up_right),
        contentDescription = null,
        tint = AppTheme.colors.primaryContent,
        modifier = Modifier.size(20.dp)
      )
      WidthSpacer(value = 6.dp)
      CircleShapeAsyncImage(
        model = targetAccount.avatar,
        modifier = Modifier.size(28.dp)
      )
      WidthSpacer(value = 4.dp)
      Text(
        text = buildAnnotatedString {
          annotateInlineEmojis(
            targetAccount.realDisplayName,
            targetAccount.emojis.toShortCode(),
            this
          )
        },
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        color = AppTheme.colors.primaryContent,
        inlineContent = inlineTextContentWithEmoji(targetAccount.emojis),
      )
    }
    BasicTextField(
      value = fieldValue,
      onValueChange = onValueChange,
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 16.dp)
        .heightIn(max = 100.dp)
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
            text = "有什么想要分享的？",
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
        modifier = Modifier.size(24.dp),
        onClick = { /*TODO*/ }
      )
      Spacer(Modifier.weight(1f))
      IconButton(
        onClick = {
          replyToStatus()
          keyboard?.hide()
        },
        enabled = fieldValue.text.isNotEmpty(),
        colors = IconButtonDefaults.filledIconButtonColors(
          containerColor = AppTheme.colors.accent,
          contentColor = Color.White,
          disabledContentColor = Color.Gray
        ),
      ) {
        when (postState) {
          is PostState.Idle, PostState.Success -> {
            Icon(
              painter = painterResource(id = R.drawable.send),
              contentDescription = null,
              modifier = Modifier.size(24.dp)
            )
          }
          is PostState.Failure -> Unit
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
    if (postState is PostState.Success) {
      focusManager.clearFocus()
    }
  }
  BackHandler(isFocused) {
    focusManager.clearFocus()
  }
}
