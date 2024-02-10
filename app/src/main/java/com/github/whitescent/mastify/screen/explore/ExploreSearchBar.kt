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

package com.github.whitescent.mastify.screen.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gigamole.composeshadowsplus.rsblur.rsBlurShadow
import com.github.whitescent.R
import com.github.whitescent.mastify.ui.component.CenterRow
import com.github.whitescent.mastify.ui.component.WidthSpacer
import com.github.whitescent.mastify.ui.theme.AppTheme

@Composable
fun ExploreSearchBar(
  text: String,
  focusRequester: FocusRequester,
  onValueChange: (String) -> Unit,
  clearText: () -> Unit,
  onFocusChange: (Boolean) -> Unit,
  navigateToSearchResult: () -> Unit
) {
  val keyboard = LocalSoftwareKeyboardController.current
  BasicTextField(
    value = text,
    onValueChange = onValueChange,
    textStyle = TextStyle(fontSize = 16.sp, color = AppTheme.colors.primaryContent),
    singleLine = true,
    cursorBrush = SolidColor(AppTheme.colors.primaryContent),
    modifier = Modifier
      .focusable()
      .focusRequester(focusRequester)
      .onFocusChanged {
        onFocusChange(it.isFocused)
      },
    keyboardActions = KeyboardActions(
      onSearch = {
        keyboard?.hide()
        navigateToSearchResult()
      }
    ),
    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search)
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .rsBlurShadow(
          radius = 12.dp,
          color = Color(0xFF000000).copy(alpha = 0.01f),
          offset = DpOffset(0.dp, 10.dp)
        )
        .clip(AppTheme.shape.betweenSmallAndMediumAvatar)
        .border(
          width = 1.dp,
          color = AppTheme.colors.exploreSearchBarBorder,
          shape = AppTheme.shape.betweenSmallAndMediumAvatar
        )
    ) {
      CenterRow(
        modifier = Modifier
          .fillMaxWidth()
          .background(AppTheme.colors.exploreSearchBarBackground)
          .clip(AppTheme.shape.betweenSmallAndMediumAvatar)
          .padding(horizontal = 12.dp, vertical = 10.dp)
      ) {
        Icon(
          painter = painterResource(id = R.drawable.search),
          contentDescription = null,
          tint = AppTheme.colors.primaryContent,
          modifier = Modifier.size(24.dp)
        )
        WidthSpacer(value = 6.dp)
        Box(contentAlignment = Alignment.CenterStart) {
          if (text.isEmpty()) {
            Text(
              text = stringResource(id = R.string.search_title),
              color = AppTheme.colors.primaryContent.copy(0.5f),
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp
            )
          }
          CenterRow(Modifier.fillMaxWidth()) {
            Box(Modifier.weight(1f)) { it() }
            if (text.isNotEmpty()) {
              Box(
                modifier = Modifier
                  .background(AppTheme.colors.cardAction, CircleShape)
                  .clickable(
                    onClick = clearText,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                  )
              ) {
                Icon(
                  painter = painterResource(id = R.drawable.close),
                  contentDescription = null,
                  modifier = Modifier.size(20.dp).padding(2.dp),
                  tint = Color.White
                )
              }
            }
          }
        }
      }
    }
  }
}
