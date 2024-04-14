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

package com.github.whitescent.mastify.screen.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.whitescent.R
import com.github.whitescent.mastify.data.preference.UserPreference
import com.github.whitescent.mastify.ui.component.ClickableIcon
import com.github.whitescent.mastify.ui.component.HeightSpacer
import com.github.whitescent.mastify.ui.component.WidthSpacer
import com.github.whitescent.mastify.ui.component.button.SwitchButton
import com.github.whitescent.mastify.ui.component.foundation.Text
import com.github.whitescent.mastify.ui.theme.AppTheme
import com.github.whitescent.mastify.utils.AppFlavorUtil
import com.github.whitescent.mastify.viewModel.SettingsViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

enum class PrivacySettings(
  val title: String,
  @StringRes val description: Int
) {
  FirebaseCrashlytics("Firebase Crashlytics", R.string.firebase_crashlytics_settings_description),
}

@Destination
@Composable
fun Settings(
  viewModel: SettingsViewModel = hiltViewModel(),
  navigator: DestinationsNavigator
) {
  val preference by viewModel.preference.collectAsStateWithLifecycle()
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(AppTheme.colors.secondaryBackground),
  ) {
    Column {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .background(AppTheme.colors.background),
      ) {
        Spacer(Modifier.statusBarsPadding())
        Box(
          Modifier
            .fillMaxWidth()
            .padding(12.dp),
        ) {
          ClickableIcon(
            painter = painterResource(id = R.drawable.arrow_left),
            tint = AppTheme.colors.primaryContent,
            modifier = Modifier
              .align(Alignment.CenterStart)
              .size(24.dp),
          ) {
            navigator.popBackStack()
          }
          Text(
            text = stringResource(id = R.string.settings_title),
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppTheme.colors.primaryContent,
            modifier = Modifier.align(Alignment.Center),
          )
        }
      }
      HeightSpacer(value = 6.dp)
      if (!AppFlavorUtil.IS_LIBRE_VARIANT) {
        PrivacySettings(
          preference = preference,
          onFirebaseCrashlyticsEnabledChange = viewModel::setFirebaseCrashlyticsEnabled
        )
      }
    }
  }
}

@Composable
private fun PrivacySettings(
  preference: UserPreference,
  onFirebaseCrashlyticsEnabledChange: (Boolean) -> Unit
) {
  Column(
    modifier = Modifier.padding(12.dp)
  ) {
    SettingsColumn(
      title = {
        Text(
          text = stringResource(id = R.string.privacy_settings_title),
          fontWeight = FontWeight.SemiBold,
          fontSize = 16.sp,
        )
      },
      modifier = Modifier.fillMaxWidth(),
    ) {
      PrivacySettings.entries.forEach {
        SettingsColumnItem(
          title = {
            Image(
              painter = painterResource(id = R.drawable.firebase_icon),
              contentDescription = null,
              modifier = Modifier.size(24.dp)
            )
            WidthSpacer(value = 6.dp)
            Text(
              text = it.title,
              fontSize = 18.sp,
              fontWeight = FontWeight.SemiBold,
              color = AppTheme.colors.primaryContent
            )
          },
          option = {
            SwitchButton(
              enabled = preference.enableFirebaseCrashlytics,
              onValueChange = onFirebaseCrashlyticsEnabledChange,
            )
          },
          content = {
            Text(
              text = stringResource(id = it.description),
              fontSize = 12.sp,
              color = AppTheme.colors.primaryContent.copy(.5f),
              textAlign = TextAlign.Start
            )
          }
        )
      }
    }
  }
}
