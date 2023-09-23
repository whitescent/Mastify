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

package com.github.whitescent.mastify.ui.component.status

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.whitescent.R
import com.github.whitescent.mastify.ui.component.CenterRow
import com.github.whitescent.mastify.ui.component.WidthSpacer
import kotlinx.coroutines.delay

@Composable
fun StatusSnackBar(
  state: StatusSnackbarState,
  modifier: Modifier = Modifier,
) {
  val snackbarType = state.currentSnackbarData?.type
  snackbarType?.let {
    val type = it
    Surface(
      shape = RoundedCornerShape(12.dp),
      color = when (type) {
        StatusSnackbarType.Text -> Color(0xFF35465E)
        StatusSnackbarType.Link -> Color(0xFF1B7CFF)
        StatusSnackbarType.Bookmark -> Color(0xFF498AE0)
        StatusSnackbarType.Error -> Color(0xFFF53232)
      },
      contentColor = Color.White,
      shadowElevation = 4.dp,
      modifier = modifier.fillMaxWidth()
    ) {
      CenterRow(Modifier.padding(horizontal = 22.dp, vertical = 16.dp)) {
        Icon(
          painter = painterResource(
            id = when (type) {
              StatusSnackbarType.Text -> R.drawable.copy_fill
              StatusSnackbarType.Link -> R.drawable.link_simple
              StatusSnackbarType.Bookmark -> R.drawable.bookmark_fill
              StatusSnackbarType.Error -> R.drawable.cloud_warning
            }
          ),
          contentDescription = null,
          modifier = Modifier.size(24.dp)
        )
        WidthSpacer(value = 8.dp)
        Text(
          text = stringResource(
            when (type) {
              StatusSnackbarType.Text -> R.string.text_copied
              StatusSnackbarType.Link -> R.string.link_copied
              StatusSnackbarType.Bookmark -> R.string.bookmarked_snackBar
              StatusSnackbarType.Error -> R.string.load_post_error
            }
          ),
          fontSize = 16.sp,
        )
      }
    }
  }
  LaunchedEffect(state.currentSnackbarData) {
    state.currentSnackbarData?.let {
      delay(2500)
      it.dismiss()
    }
  }
}
