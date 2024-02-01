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

package com.github.whitescent.mastify.screen.login

import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.whitescent.R
import com.github.whitescent.mastify.LoginNavGraph
import com.github.whitescent.mastify.ui.component.CenterRow
import com.github.whitescent.mastify.ui.component.ClickableIcon
import com.github.whitescent.mastify.ui.component.HeightSpacer
import com.github.whitescent.mastify.ui.component.WidthSpacer
import com.github.whitescent.mastify.utils.launchCustomChromeTab
import com.github.whitescent.mastify.viewModel.LoginStatus
import com.github.whitescent.mastify.viewModel.LoginViewModel
import com.ramcosta.composedestinations.annotation.Destination

@OptIn(ExperimentalComposeUiApi::class)
@LoginNavGraph(start = true)
@Destination(route = "login_route")
@Composable
fun Login(
  viewModel: LoginViewModel = hiltViewModel()
) {
  val context = LocalContext.current
  val state = viewModel.uiState
  val instanceVerifyErrorMsg = stringResource(id = R.string.instance_verification_error)

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFF3F4366))
      .statusBarsPadding()
      .padding(35.dp)
      .semantics { testTagsAsResourceId = true }
  ) {
    Column(
      modifier = Modifier.fillMaxWidth()
    ) {
      HeightSpacer(value = 70.dp)
      CenterRow {
        Image(
          painter = painterResource(id = R.drawable.logo),
          contentDescription = null,
          modifier = Modifier.size(90.dp),
        )
        WidthSpacer(value = 16.dp)
        Icon(
          painter = painterResource(id = R.drawable.logo_text),
          contentDescription = null,
          modifier = Modifier.size(180.dp),
          tint = Color.White
        )
      }
      HeightSpacer(value = 24.dp)
      BasicTextField(
        value = state.text,
        onValueChange = viewModel::onValueChange,
        cursorBrush = SolidColor(Color.White),
        textStyle = TextStyle(color = Color.White, fontSize = 30.sp),
        singleLine = true,
        modifier = Modifier.semantics {
          testTagsAsResourceId = true
          testTag = "domain input"
        }
      ) {
        CenterRow {
          Box(Modifier.weight(1f)) {
            if (state.text.isEmpty()) {
              Text(
                text = stringResource(id = R.string.instance_address_tip),
                color = Color.White.copy(0.5f),
                fontSize = 24.sp
              )
            }
            it()
          }
          if (state.text.isNotEmpty()) {
            ClickableIcon(
              painter = painterResource(id = R.drawable.close),
              tint = Color.White,
              interactiveSize = 24.dp,
              onClick = viewModel::clearInputText
            )
          }
        }
      }
      HorizontalDivider(color = Color.White, modifier = Modifier.padding(top = 16.dp))
      HeightSpacer(value = 12.dp)
      AnimatedVisibility(
        visible = (viewModel.instanceLocalError && state.text.isNotEmpty()) ||
          state.loginStatus is LoginStatus.Failure,
        enter = scaleIn(tween(150, easing = LinearEasing), 0.5f),
        exit = scaleOut(),
        modifier = Modifier.padding(bottom = 24.dp)
      ) {
        Text(
          text = if (state.loginStatus is LoginStatus.Failure)
            stringResource(R.string.failed_to_retrieve_instance)
          else stringResource(R.string.error_invalid_domain),
          fontSize = 18.sp,
          color = Color(0xFFFF3838)
        )
      }
      AnimatedVisibility(
        visible = state.text.isNotEmpty(),
        enter = scaleIn(tween(150, easing = LinearEasing), 0.5f),
        exit = scaleOut()
      ) {
        Button(
          onClick = {
            viewModel.checkInstance()
          },
          modifier = Modifier.fillMaxWidth().semantics { testTag = "login button" },
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.elevatedButtonColors(
            contentColor = Color.White,
            containerColor = Color(0xFF378BED),
            disabledContainerColor = Color(0xFFDADADA)
          ),
          enabled = !viewModel.instanceLocalError
        ) {
          when (state.loginStatus) {
            LoginStatus.Idle, LoginStatus.Failure -> {
              Text(
                text = stringResource(R.string.log_in_to, state.text),
                modifier = Modifier.padding(vertical = 6.dp).animateContentSize(),
                fontSize = 18.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
            LoginStatus.Loading -> {
              CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp)
            }
          }
        }
      }
    }
  }

  LaunchedEffect(state.authenticateError, state.clientId) {
    if (state.authenticateError) {
      Toast.makeText(context, instanceVerifyErrorMsg, Toast.LENGTH_LONG).show()
    }
    if (state.clientId.isNotEmpty()) {
      launchCustomChromeTab(
        context = context,
        uri = Uri.parse(
          "https://${state.text}/oauth/authorize?client_id=${state.clientId}" +
            "&scope=read+write+push" +
            "&redirect_uri=mastify://oauth" +
            "&response_type=code"
        ),
        toolbarColor = Color(0xFF3F4366).toArgb()
      )
    }
  }
}
