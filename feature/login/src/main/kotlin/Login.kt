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

package com.github.whitescent.mastify.feature.login

import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.gigamole.composeshadowsplus.rsblur.rsBlurShadow
import com.github.whitescent.mastify.core.common.compose.LaunchedValueEffect
import com.github.whitescent.mastify.core.common.launchChromeTabs
import com.github.whitescent.mastify.core.common.strings.CommonStrings
import com.github.whitescent.mastify.core.navigation.Route
import com.github.whitescent.mastify.core.ui.AppTheme
import com.github.whitescent.mastify.core.ui.Color
import com.github.whitescent.mastify.core.ui.component.CenterRow
import com.github.whitescent.mastify.core.ui.component.HeightSpacer
import com.github.whitescent.mastify.core.ui.component.WidthSpacer
import com.github.whitescent.mastify.core.ui.component.rememberDialogState
import com.github.whitescent.mastify.core.ui.shape.SmoothCornerShape

@Composable
internal fun Login(
  navController: NavController,
  viewModel: LoginViewModel = hiltViewModel()
) {
  val context = LocalContext.current
  val state = viewModel.uiState
  val instanceVerifyErrorMsg = stringResource(id = CommonStrings.instance_verification_error)
  val dialogState = rememberDialogState()

  Box(
    modifier = Modifier
      .fillMaxSize()
      .consumeWindowInsets(PaddingValues(bottom = 200.dp))
      .background(AppTheme.colors.bottomSheetBackground),
  ) {
    Image(
      painter = painterResource(id = R.drawable.login_background),
      contentDescription = null,
      modifier = Modifier.fillMaxSize(),
      contentScale = ContentScale.Crop
    )
    Column(
      modifier = Modifier
        .align(Alignment.Center)
        .imePadding()
        .padding(horizontal = 64.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      CenterRow {
        Image(
          painter = painterResource(id = R.drawable.logo),
          contentDescription = null,
          modifier = Modifier
            .rsBlurShadow(
              radius = 4.dp,
              color = Color(0xFF4F61FE).copy(alpha = 0.46f),
              offset = DpOffset(0.dp, 0.dp),
              shape = AppTheme.shape.mediumAvatar,
              spread = 0.2.dp
            )
            .clip(AppTheme.shape.mediumAvatar)
            .size(80.dp)
        )
        WidthSpacer(value = 16.dp)
        Icon(
          painter = painterResource(id = R.drawable.logo_text),
          contentDescription = null,
          tint = Color(0xFF5C72FF),
          modifier = Modifier.size(150.dp)
        )
      }
      Text(
        text = stringResource(CommonStrings.app_description),
        color = AppTheme.colors.primaryContent.copy(0.5f),
        fontWeight = FontWeight.Light,
        fontSize = 14.sp,
        letterSpacing = 1.5.sp
      )
      HeightSpacer(value = 50.dp)
      BasicTextField(
        state = viewModel.loginInput,
        lineLimits = TextFieldLineLimits.SingleLine,
        modifier = Modifier
          .clip(SmoothCornerShape(14.dp))
          .background(if (isSystemInDarkTheme()) Color(0xFF575B7A) else Color.White)
          .border(1.3.dp, Color(0xFF79B2FF), SmoothCornerShape(14.dp))
          .testTag("domain input"),
        cursorBrush = SolidColor(AppTheme.colors.primaryContent),
        textStyle = TextStyle(color = AppTheme.colors.primaryContent, fontSize = 16.sp),
        decorator = {
          CenterRow(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 24.dp, vertical = 14.dp)
          ) {
            Icon(
              painter = painterResource(id = R.drawable.link_simple),
              contentDescription = null,
              tint = if (isSystemInDarkTheme()) Color(0xFFFAFAFA) else Color(0xFF046FFF),
              modifier = Modifier.size(24.dp)
            )
            Box(
              modifier = Modifier
                .padding(horizontal = 12.dp)
                .height(20.dp)
                .width(1.3.dp)
                .background(Color(0xFFD7D7D7))
            )
            Box(contentAlignment = Alignment.CenterStart) {
              if (viewModel.loginInput.text.isEmpty()) {
                Text(
                  text = stringResource(id = CommonStrings.instance_address_tip),
                  color = Color(0xFF9CA0BA),
                  fontSize = 16.sp,
                  maxLines = 1
                )
              }
              it()
            }
          }
        }
      )
      AnimatedVisibility(
        visible = viewModel.instanceLocalError && viewModel.loginInput.text.isNotEmpty() ||
          state.loginStatus is LoginStatus.Failure,
        enter = slideInVertically { -it } + fadeIn(),
        exit = slideOutVertically { -it } + fadeOut(),
        modifier = Modifier
          .align(Alignment.Start)
          .padding(top = 24.dp)
      ) {
        Text(
          text = if (state.loginStatus is LoginStatus.Failure)
            stringResource(CommonStrings.failed_to_retrieve_instance)
          else stringResource(CommonStrings.error_invalid_domain),
          fontSize = 14.sp,
          fontWeight = FontWeight.SemiBold,
          color = Color(0xFFFF3838).copy(.6f)
        )
      }
      HeightSpacer(value = 24.dp)
      Button(
        onClick = viewModel::checkInstance,
        shape = SmoothCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
          contentColor = Color.White,
          containerColor = Color(Color(0xFF2890DC), Color(0xFF6094FA).copy(.44f)),
          disabledContainerColor = Color.Gray.copy(.5f),
          disabledContentColor = Color.White.copy(.5f)
        ),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("login button"),
        enabled = !viewModel.instanceLocalError
      ) {
        when (state.loginStatus) {
          LoginStatus.Idle, LoginStatus.Failure -> {
            Text(
              text = when (viewModel.loginInput.text.isEmpty()) {
                true -> stringResource(CommonStrings.log_in_title)
                else -> stringResource(CommonStrings.log_in_to, viewModel.loginInput.text)
              },
              modifier = Modifier
                .padding(vertical = 6.dp)
                .animateContentSize(),
              fontSize = 18.sp,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
          LoginStatus.Loading -> CircularProgressIndicator(
            color = Color.White,
            strokeWidth = 3.dp,
            modifier = Modifier.size(32.dp)
          )
          else -> Unit
        }
      }
    }
    Text(
      text = "${stringResource(id = CommonStrings.app_name)}\n" +
        "${BuildConfig.VERSION_NAME}  ${BuildConfig.BUILD_TYPE}",
      fontSize = 14.sp,
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(WindowInsets.navigationBars.asPaddingValues()),
      color = AppTheme.colors.primaryContent.copy(.35f),
      textAlign = TextAlign.Center,
    )
  }
  LaunchedEffect(state.authenticateError, state.clientId) {
    if (state.authenticateError) {
      Toast.makeText(context, instanceVerifyErrorMsg, Toast.LENGTH_LONG).show()
    }
    if (state.clientId.isNotEmpty()) {
      launchChromeTabs(
        context = context,
        uri = Uri.parse(
          "https://${viewModel.loginInput.text}/oauth/authorize?client_id=${state.clientId}" +
            "&scope=$CLIENT_SCOPES" +
            "&redirect_uri=$REDIRECT_URIS" +
            "&response_type=code"
        )
      )
    }
  }
  LoginDialog(dialogState)
  LaunchedValueEffect(viewModel.code) {
    if (it != null) dialogState.show()
  }
  LaunchedValueEffect(state.fetchedAccount) {
    if (it != null) {
      navController.navigate(Route.Foundation) {
        popUpTo<Route.Login> {
          inclusive = true
        }
      }
    }
  }
}

private const val REDIRECT_URIS = "mastify://oauth"
private const val CLIENT_SCOPES = "read write push"
