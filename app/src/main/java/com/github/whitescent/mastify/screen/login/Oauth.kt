/*
 * Copyright 2023 WhiteScent
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

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.whitescent.R
import com.github.whitescent.mastify.LoginNavGraph
import com.github.whitescent.mastify.screen.NavGraphs
import com.github.whitescent.mastify.ui.component.CenterRow
import com.github.whitescent.mastify.ui.component.WidthSpacer
import com.github.whitescent.mastify.ui.theme.AppTheme
import com.github.whitescent.mastify.ui.transitions.OauthTransitions
import com.github.whitescent.mastify.viewModel.OauthViewModel
import com.ramcosta.composedestinations.annotation.DeepLink
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.popUpTo
import kotlinx.coroutines.delay

@Composable
@LoginNavGraph
@Destination(
  deepLinks = [
    DeepLink(uriPattern = "mastify://oauth?code={code}")
  ],
  style = OauthTransitions::class
)
fun Oauth(
  navigator: DestinationsNavigator,
  viewModel: OauthViewModel = hiltViewModel()
) {
  val activity = (LocalContext.current as? Activity)

  Dialog(
    onDismissRequest = { },
    properties = DialogProperties(
      usePlatformDefaultWidth = false
    )
  ) {
    Surface(
      shape = RoundedCornerShape(12.dp),
      color = AppTheme.colors.background,
      shadowElevation = 6.dp
    ) {
      CenterRow(
        modifier = Modifier.padding(24.dp)
      ) {
        Text(text = stringResource(id = R.string.title_connecting), color = AppTheme.colors.primaryContent)
        WidthSpacer(value = 10.dp)
        CircularProgressIndicator(
          modifier = Modifier.size(24.dp),
          strokeWidth = 2.dp,
          color = AppTheme.colors.accent
        )
      }
    }
  }
  LaunchedEffect(Unit) {
    delay(300)
    viewModel.code?.let {
      viewModel.fetchAccessToken()
    } ?: run {
      // If the user refuses OAuth, we need to navigate to the login screen
      navigator.popBackStack()
    }
    viewModel.navigateFlow.collect {
      if (it) {
        navigator.navigate(NavGraphs.app) {
          popUpTo(NavGraphs.root) {
            inclusive = true
          }
        }
      }
    }
  }
  BackHandler {
    activity?.finish()
  }
}
