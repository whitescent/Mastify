package com.github.whitescent.mastify.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Divider
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.whitescent.R
import com.github.whitescent.mastify.ui.theme.AppTheme

@Composable
fun ReplyTextField(
  avatar: String,
  text: String,
  onValueChange: (String) -> Unit
) {
  var expand by rememberSaveable { mutableStateOf(false) }
  val focusRequester = remember { FocusRequester() }
  Column(Modifier.imePadding()) {
    Divider()
    Surface(
      modifier = Modifier.fillMaxWidth(),
      color = AppTheme.colors.cardBackground
    ) {
      Column(Modifier.navigationBarsPadding().padding(16.dp)) {
        TextField(
          value = text,
          onValueChange = onValueChange,
          modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 100.dp)
            .focusRequester(focusRequester)
            .onFocusChanged {
              expand = it.isFocused
            },
          leadingIcon = {
            CircleShapeAsyncImage(model = avatar, modifier = Modifier.size(36.dp))
          },
          colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = AppTheme.colors.cardAction,
            cursorColor = AppTheme.colors.primaryContent
          ),
          textStyle = TextStyle(fontSize = 16.sp, color = AppTheme.colors.primaryContent),
          placeholder = {
            Text(
              text = "有什么想要分享的？",
              color = Color.Gray
            )
          }
        )
        HeightSpacer(value = 4.dp)
        AnimatedVisibility(
          visible = expand
        ) {
          CenterRow(Modifier.fillMaxWidth()) {
            IconButton(
              onClick = { /*TODO*/ }
            ) {
              Icon(
                painter = painterResource(id = R.drawable.image),
                contentDescription = null,
                tint = AppTheme.colors.cardAction,
                modifier = Modifier.size(24.dp)
              )
            }
            WidthSpacer(value = 4.dp)
            IconButton(
              onClick = { /*TODO*/ }
            ) {
              Icon(
                painter = painterResource(id = R.drawable.emoji),
                contentDescription = null,
                tint = AppTheme.colors.cardAction,
                modifier = Modifier.size(24.dp)
              )
            }
            Spacer(Modifier.weight(1f))
            Button(
              onClick = { /*TODO*/ },
              colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.accent),
              shape = CircleShape
            ) {
              CenterRow {
                Text(text = "发送", color = AppTheme.colors.background, fontSize = 16.sp)
                WidthSpacer(value = 6.dp)
                Icon(
                  painter = painterResource(id = R.drawable.send),
                  contentDescription = null,
                  tint = AppTheme.colors.background,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          }
        }
      }
    }
  }
}
