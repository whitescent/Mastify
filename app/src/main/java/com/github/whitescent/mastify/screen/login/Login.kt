package com.github.whitescent.mastify.screen.login

import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.github.whitescent.R
import com.github.whitescent.mastify.LoginNavGraph
import com.github.whitescent.mastify.ui.component.CenterRow
import com.github.whitescent.mastify.ui.component.ClickableIcon
import com.github.whitescent.mastify.ui.component.HeightSpacer
import com.github.whitescent.mastify.ui.component.WidthSpacer
import com.github.whitescent.mastify.ui.theme.AppTheme
import com.github.whitescent.mastify.ui.transitions.LoginTransitions
import com.github.whitescent.mastify.utils.launchCustomChromeTab
import com.github.whitescent.mastify.viewModel.LoginViewModel
import com.ramcosta.composedestinations.annotation.Destination

@LoginNavGraph(start = true)
@Destination(style = LoginTransitions::class, route = "login_route")
@Composable
fun Login(
  viewModel: LoginViewModel = hiltViewModel()
) {
  val context = LocalContext.current
  val backgroundColor = AppTheme.colors.primaryContent.toArgb()
  val state = viewModel.uiState
  val instance by viewModel.instance.collectAsStateWithLifecycle()
  val instanceVerifyErrorMsg = stringResource(id = R.string.instance_verification_error)

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(AppTheme.colors.secondaryContent)
      .statusBarsPadding()
      .padding(30.dp),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth()
    ) {
      Icon(
        painter = painterResource(id = R.drawable.logo_text),
        contentDescription = null,
        modifier = Modifier.size(180.dp).border(5.dp, Color.Red),
        tint = AppTheme.colors.primaryContent
      )
      BasicTextField(
        value = state.text,
        onValueChange = viewModel::onValueChange,
        modifier = Modifier.clip(RoundedCornerShape(12.dp)),
        cursorBrush = SolidColor(AppTheme.colors.primaryContent),
        textStyle = TextStyle(color = AppTheme.colors.primaryContent, fontSize = 16.sp),
        singleLine = true
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(AppTheme.colors.cardBackground)
        ) {
          Column(Modifier.padding(12.dp)) {
            Text(
              text = stringResource(id = R.string.instance_address),
              color = AppTheme.colors.primaryContent,
              fontSize = 12.sp,
              modifier = Modifier.padding(horizontal = 6.dp),
            )
            HeightSpacer(value = 6.dp)
            CenterRow(Modifier.fillMaxWidth()) {
              Icon(
                painter = painterResource(id = R.drawable.globe),
                contentDescription = null,
                tint = AppTheme.colors.primaryContent,
                modifier = Modifier.size(24.dp)
              )
              WidthSpacer(value = 4.dp)
              Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                it()
                Crossfade(targetState = state.text.isEmpty()) {
                  when (it) {
                    true -> {
                      Text(
                        text = stringResource(id = R.string.instance_address_tip),
                        color = AppTheme.colors.primaryContent.copy(0.5f)
                      )
                    }
                    else -> Unit
                  }
                }
              }
              Crossfade(targetState = state.text.isNotEmpty()) {
                when (it) {
                  true -> {
                    ClickableIcon(
                      painter = painterResource(id = R.drawable.close),
                      tint = AppTheme.colors.primaryContent,
                      modifier = Modifier.size(20.dp),
                      onClick = viewModel::clearInputText
                    )
                  }
                  else -> Unit
                }
              }
            }
          }
        }
      }
      AnimatedVisibility(
        visible = state.text.isNotEmpty(),
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier.padding(6.dp)
      ) {
        Column {
          HeightSpacer(value = 6.dp)
          if (viewModel.instanceLocalError) {
            Text(
              text = stringResource(R.string.error_invalid_domain),
              fontSize = 14.sp,
              color = Color(0xFFFF3838),
            )
          } else {
            Crossfade(state.isTyping) {
              when (it) {
                true -> {
                  CircularProgressIndicator(
                    color = AppTheme.colors.primaryContent,
                    modifier = Modifier.size(24.dp)
                  )
                }
                else -> {
                  instance?.let { result ->
                    InstanceCard(
                      title = result.instanceTitle,
                      description = result.instanceDescription,
                      activeMonth = result.activeMonth,
                      imageUrl = result.instanceImageUrl,
                      onClick = { name ->
                        viewModel.authenticateApp(
                          appName = name,
                          navigateToOauth = { clientId ->
                            launchCustomChromeTab(
                              context = context,
                              uri = Uri.parse(
                                "https://${state.text}/oauth/authorize?client_id=$clientId" +
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
                  } ?: run {
                    Text(
                      text = stringResource(R.string.failed_to_retrieve_instance),
                      fontSize = 14.sp,
                      color = Color(0xFFFF3838),
                    )
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  LaunchedEffect(state.authenticateError) {
    if (state.authenticateError) {
      Toast.makeText(context, instanceVerifyErrorMsg, Toast.LENGTH_LONG).show()
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstanceCard(
  title: String,
  description: String,
  activeMonth: Int,
  imageUrl: String,
  onClick: (String) -> Unit
) {
  val appName = stringResource(id = R.string.app_name)
  Card(
    modifier = Modifier.fillMaxWidth(),
    elevation = CardDefaults.cardElevation(
      defaultElevation = 6.dp
    ),
    onClick = {
      onClick(appName)
    },
    colors = CardDefaults.cardColors(containerColor = AppTheme.colors.cardBackground),
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
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      HeightSpacer(value = 4.dp)
      Text(
        text = title,
        fontSize = 18.sp,
        color = AppTheme.colors.primaryContent
      )
      HeightSpacer(value = 10.dp)
      Text(
        text = description,
        fontSize = 14.sp,
        color = AppTheme.colors.primaryContent
      )
      if (activeMonth != 0) {
        CenterRow(
          modifier = Modifier
            .align(Alignment.End)
            .padding(12.dp)
        ) {
          Text(
            text = stringResource(id = R.string.users_of_instance),
            color = Color.Gray
          )
          WidthSpacer(value = 4.dp)
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = AppTheme.colors.cardAction
          ) {
            CenterRow(Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
              Icon(
                painter = painterResource(id = R.drawable.users),
                contentDescription = null,
                tint = AppTheme.colors.primaryContent,
                modifier = Modifier.size(20.dp)
              )
              WidthSpacer(value = 2.dp)
              Text(
                text = activeMonth.toString(),
                color = AppTheme.colors.primaryContent
              )
            }
          }
        }
      }
    }
  }
}
