@file:Suppress("CrossfadeLabel")

package com.github.whitescent.mastify.screen.login

import android.app.Activity
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.github.whitescent.R
import com.github.whitescent.mastify.LoginNavGraph
import com.github.whitescent.mastify.ui.component.CenterRow
import com.github.whitescent.mastify.ui.component.HeightSpacer
import com.github.whitescent.mastify.ui.component.WidthSpacer
import com.github.whitescent.mastify.ui.theme.AppTheme
import com.github.whitescent.mastify.ui.transitions.LoginTransitions
import com.github.whitescent.mastify.utils.launchCustomChromeTab
import com.ramcosta.composedestinations.annotation.Destination

@LoginNavGraph(start = true)
@Destination(style = LoginTransitions::class)
@Composable
fun LoginScreen(
  viewModel: LoginViewModel = hiltViewModel()
) {

  val context = LocalContext.current
  val activity = (context as? Activity)
  val backgroundColor = MaterialTheme.colorScheme.background.toArgb()
  val state = viewModel.uiState

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(AppTheme.colors.background)
      .statusBarsPadding()
      .padding(30.dp),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth()
    ) {
      CenterRow {
        Surface(
          shape = CircleShape,
          modifier = Modifier.size(48.dp)
        ) {
          // Image(
          //   painter = painterResource(id = R.drawable.logo),
          //   contentDescription = null
          // )
        }
        WidthSpacer(value = 12.dp)
        Text(
          text = stringResource(id = R.string.app_name),
          color = AppTheme.colors.primaryContent
        )
      }
      HeightSpacer(value = 12.dp)
      Text(
        text = "登录",
        color = AppTheme.colors.primaryContent
      )
      HeightSpacer(value = 6.dp)
      Text(
        text = "请先输入您的实例服务器",
        color = AppTheme.colors.primaryContent
      )
      HeightSpacer(value = 16.dp)
      Text(
        text = "实例地址",
        color = AppTheme.colors.primaryContent
      )
      HeightSpacer(value = 4.dp)
      OutlinedTextField(
        value = state.text,
        onValueChange = viewModel::onValueChange,
        leadingIcon = {
          Text(
            text = "https://",
            modifier = Modifier.padding(start = 8.dp)
          )
        },
        trailingIcon = {
          AnimatedVisibility(state.text.isNotEmpty()) {
            IconButton(
              onClick = viewModel::clearInputText
            ) {
              Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = null,
//                tint = AppTheme.colorScheme.primary
              )
            }
          }
        },
        modifier = Modifier.fillMaxWidth(),
        colors = TextFieldDefaults.colors(
          focusedContainerColor = Color.Transparent,
          unfocusedContainerColor = Color.Transparent,
          disabledContainerColor = Color.Transparent
        ),
        isError = state.instanceError && !state.isTyping,
        singleLine = true
      )
      HeightSpacer(value = 8.dp)
      AnimatedVisibility(
        visible = state.text.isNotEmpty(),
        enter = fadeIn(),
        exit = fadeOut()
      ) {
        Crossfade (state.isTyping) {
          when (it) {
            true -> {
              CircularProgressIndicator(
                color = AppTheme.colors.primaryContent,
                modifier = Modifier.size(24.dp)
              )
            }
            else -> {
              Crossfade(state.instanceError) { error ->
                when (error) {
                  true -> Text(
                    text = "当前实例不存在",
                  )
                  else -> InstanceCard(
                    title = state.instanceTitle,
                    description = state.instanceDescription,
                    imageUrl = state.instanceImageUrl,
                    onClick = { name ->
                      viewModel.authenticateApp(
                        appName = name,
                        navigateToOauth = { clientId ->
                          launchCustomChromeTab(
                            context = context,
                            uri = Uri.parse(
                              "https://${state.text}/oauth/authorize?client_id=${clientId}" +
                                "&scope=read+write+push" +
                                "&redirect_uri=mastify://oauth" +
                                "&response_type=code"
                            ),
                            toolbarColor = backgroundColor
                          )
                        }
                      )
                    }
                  )
                }
              }
            }
          }
        }
      }
    }
  }

  if (state.openDialog) ProcessDialog()

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstanceCard(
  title: String,
  description: String,
  imageUrl: String,
  onClick: (String) -> Unit
) {
  val appName = stringResource(id = R.string.app_name)
  Card(
    elevation = CardDefaults.cardElevation(
      defaultElevation = 6.dp
    ),
    onClick = {
      onClick(appName)
    }
  ) {
    AsyncImage(
      model = imageUrl,
      contentDescription = null,
      modifier = Modifier
        .fillMaxWidth()
        .height(150.dp),
      contentScale = ContentScale.Crop
    )
    Column(
      modifier = Modifier.padding(12.dp)
    ) {
      HeightSpacer(value = 12.dp)
      Text(
        text = title,
//        style = AppTheme.typography.titleLarge
      )
      HeightSpacer(value = 12.dp)
      Text(
        text = description,
//        style = AppTheme.typography.titleSmall
      )
    }
  }
}

@Composable
fun ProcessDialog() {
  Dialog(
    onDismissRequest = {  },
    properties = DialogProperties(
      usePlatformDefaultWidth = false
    )
  ) {
    Surface(
      shape = RoundedCornerShape(12.dp),
      color = Color.White,
      shadowElevation = 6.dp
    ) {
      CenterRow(
        modifier = Modifier.padding(24.dp)
      ) {
        Text(text = "正在请求授权")
        WidthSpacer(value = 10.dp)
        CircularProgressIndicator(modifier = Modifier.size(24.dp))
      }
    }
  }
}
