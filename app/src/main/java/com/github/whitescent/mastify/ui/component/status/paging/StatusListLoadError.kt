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

package com.github.whitescent.mastify.ui.component.status.paging

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.github.whitescent.R
import com.github.whitescent.mastify.ui.component.HeightSpacer
import com.github.whitescent.mastify.ui.theme.AppTheme

@Composable
fun StatusListLoadError(
  errorMessage: String? = null,
  retry: () -> Unit
) {
  val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.error))
  val progress by animateLottieCompositionAsState(
    composition = composition,
    iterations = LottieConstants.IterateForever
  )
  Box(
    modifier = Modifier.fillMaxSize(),
    contentAlignment = Alignment.Center
  ) {
    Column(
      modifier = Modifier.width(320.dp).padding(bottom = 150.dp),
      horizontalAlignment = Alignment.Start
    ) {
      LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = Modifier.height(220.dp).fillMaxWidth(),
        contentScale = ContentScale.Crop
      )
      HeightSpacer(value = 18.dp)
      Text(
        text = stringResource(id = R.string.oops_something_went_wrong),
        fontWeight = FontWeight.ExtraBold,
        fontSize = 23.sp,
        color = AppTheme.colors.primaryContent
      )
      if (errorMessage != null) {
        HeightSpacer(value = 6.dp)
        Text(
          text = errorMessage,
          fontWeight = FontWeight.Medium,
          fontSize = 16.sp,
          color = AppTheme.colors.primaryContent.copy(0.7f)
        )
      }
      HeightSpacer(value = 16.dp)
      Button(
        onClick = retry,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
          containerColor = AppTheme.colors.accent
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
      ) {
        Text(
          text = stringResource(id = R.string.retry),
          fontSize = 16.sp,
          fontWeight = FontWeight.Medium
        )
      }
    }
  }
}
