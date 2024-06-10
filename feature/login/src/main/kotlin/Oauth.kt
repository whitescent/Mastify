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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.github.whitescent.mastify.core.common.compose.LocalActivity
import com.github.whitescent.mastify.core.common.strings.CommonStrings
import com.github.whitescent.mastify.core.ui.AppTheme
import com.github.whitescent.mastify.core.ui.component.CenterRow
import com.github.whitescent.mastify.core.ui.component.WidthSpacer

@Composable
fun Oauth(
  navController: NavController,
  viewModel: LoginViewModel = hiltViewModel()
) {
  val activity = LocalActivity.current

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
        Text(
          text = stringResource(id = CommonStrings.title_connecting),
          color = AppTheme.colors.primaryContent
        )
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
//    viewModel.code?.let {
//      viewModel.fetchAccessToken()
//    } ?: run {
//      // If the user refuses OAuth, we need to navigate to the login screen
//      navController.popBackStack()
//    }
//    viewModel.navigateFlow.collect {
//      navController.navigate(Route.App) {
//        popUpTo(Route.Login) {
//          inclusive = true
//        }
//      }
//    }
  }
  BackHandler {
    activity?.finish()
  }
}
