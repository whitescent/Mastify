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

package com.github.whitescent.mastify.screen.post

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.whitescent.R
import com.github.whitescent.mastify.data.model.ui.StatusUiData.Visibility
import com.github.whitescent.mastify.ui.component.CenterRow
import com.github.whitescent.mastify.ui.component.HeightSpacer
import com.github.whitescent.mastify.ui.component.WidthSpacer
import com.github.whitescent.mastify.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostVisibilitySheet(
  sheetState: SheetState,
  currentVisibility: Visibility,
  onVisibilityUpdated: (Visibility) -> Unit,
  onDismissRequest: () -> Unit,
) {
  ModalBottomSheet(
    onDismissRequest = onDismissRequest,
    sheetState = sheetState,
    windowInsets = WindowInsets.statusBars,
    containerColor = AppTheme.colors.bottomSheetBackground,
  ) {
    Column(Modifier.padding(vertical = 10.dp)) {
      Text(
        text = stringResource(id = R.string.change_post_visibility),
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.align(Alignment.CenterHorizontally),
        color = AppTheme.colors.primaryContent,
      )
      HeightSpacer(value = 6.dp)
      Text(
        text = stringResource(id = R.string.visibility_description),
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
          .align(Alignment.CenterHorizontally)
          .padding(horizontal = 48.dp),
        color = Color.Gray,
      )
      HeightSpacer(value = 6.dp)
      Column(Modifier.padding(horizontal = 12.dp)) {
        Visibility.entries.forEach {
          PostVisibilityItem(
            selected = it == currentVisibility,
            visibility = it,
            onClick = { onVisibilityUpdated(it) },
          )
          if (it != Visibility.entries.last()) HeightSpacer(value = 6.dp)
        }
      }
    }
  }
}

@Composable
private fun PostVisibilityItem(
  selected: Boolean,
  visibility: Visibility,
  onClick: () -> Unit,
) {
  if (visibility == Visibility.Unknown) return
  Surface(
    shape = RoundedCornerShape(14.dp),
    border = if (selected) BorderStroke((2.5).dp, AppTheme.colors.bottomSheetSelectedBorder) else null,
    color = when (selected) {
      true -> AppTheme.colors.bottomSheetItemSelectedBackground
      else -> AppTheme.colors.bottomSheetItemBackground
    },
    onClick = onClick,
    modifier = Modifier.fillMaxWidth()
  ) {
    CenterRow(Modifier.padding(horizontal = 16.dp, vertical = 20.dp)) {
      CenterRow(Modifier.weight(1f)) {
        Icon(
          painter = when (visibility) {
            Visibility.Public -> painterResource(R.drawable.globe)
            Visibility.Unlisted -> painterResource(R.drawable.lock_open)
            Visibility.Private -> painterResource(R.drawable.lock)
            Visibility.Direct -> painterResource(R.drawable.at)
            else -> throw IllegalArgumentException("Invalid visibility: $visibility")
          },
          contentDescription = null,
          tint = AppTheme.colors.primaryContent,
          modifier = Modifier.size(24.dp)
        )
        WidthSpacer(value = 4.dp)
        Text(
          text = stringResource(
            id = when (visibility) {
              Visibility.Public -> R.string.visibility_public
              Visibility.Unlisted -> R.string.visibility_unlisted
              Visibility.Private -> R.string.visibility_private
              Visibility.Direct -> R.string.visibility_direct
              else -> throw IllegalArgumentException("Invalid visibility: $visibility")
            }
          ),
          color = AppTheme.colors.primaryContent,
          fontWeight = FontWeight.Medium,
          fontSize = 16.sp,
        )
      }
      if (selected) {
        Icon(
          painter = painterResource(id = R.drawable.check_border_0_5),
          contentDescription = null,
          modifier = Modifier.size(20.dp),
          tint = AppTheme.colors.bottomSheetItemSelectedIcon
        )
      }
    }
  }
}
