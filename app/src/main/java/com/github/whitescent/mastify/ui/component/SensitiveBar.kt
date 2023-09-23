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

package com.github.whitescent.mastify.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.whitescent.R

@Composable
fun SensitiveBar(
  spoilerText: String,
  onClick: () -> Unit,
) {
  Surface(
    modifier = Modifier.fillMaxWidth(),
    color = Color(0xFF7E7E7E),
    shape = RoundedCornerShape(10.dp),
    onClick = onClick
  ) {
    Row(Modifier.padding(20.dp)) {
      Icon(
        painter = painterResource(id = R.drawable.eye_hide),
        contentDescription = null,
        tint = sensitiveContentColor,
        modifier = Modifier.size(24.dp)
      )
      WidthSpacer(value = 8.dp)
      Column {
        Text(
          text = spoilerText,
          color = sensitiveContentColor,
          fontSize = 16.sp
        )
        HeightSpacer(value = 6.dp)
        Text(
          text = stringResource(id = R.string.click_to_display_content),
          color = sensitiveContentColor,
          fontSize = 14.sp,
        )
      }
    }
  }
}

private val sensitiveContentColor = Color(0xFFCACACA)
