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

package com.github.whitescent.mastify.ui.component

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import com.github.whitescent.mastify.ui.theme.AppTheme

/**
 * This is a temporary workaround
 * https://issuetracker.google.com/issues/139320238
 */
@Composable
fun LocalizedClickableText(
  @StringRes stringRes: Int,
  highlightText: String,
  modifier: Modifier = Modifier,
  fontSize: TextUnit = TextUnit.Unspecified,
  style: TextStyle = TextStyle.Default.copy(fontSize = fontSize),
  onClick: () -> Unit,
) {
  val text = stringResource(stringRes, highlightText)
  val formattedString = text.format(highlightText)
  val annotatedText = buildAnnotatedString {
    val startIndex = formattedString.indexOf(highlightText)
    val endIndex = startIndex + highlightText.length

    append(formattedString.substring(0, startIndex))
    withStyle(style = SpanStyle(color = AppTheme.colors.accent)) {
      pushStringAnnotation(tag = "URL", annotation = highlightText)
      append(formattedString.substring(startIndex, endIndex))
      pop()
    }

    if (endIndex < formattedString.length) {
      append(formattedString.substring(endIndex))
    }
  }

  Text(
    text = annotatedText,
    fontSize = fontSize,
    style = style,
    modifier = modifier.clickable(
      onClick = onClick,
      indication = null,
      interactionSource = remember { MutableInteractionSource() }
    ),
    color = AppTheme.colors.primaryContent.copy(0.6f)
  )
}
