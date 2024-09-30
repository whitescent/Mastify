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

package com.github.whitescent.mastify.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.github.whitescent.mastify.core.common.compose.noCompositionLocalProvided

@Immutable
data class MastifyColorScheme(
  val primaryContent: Color,
  val primaryGradient: Brush,
  val accent: Color,
  val accent10: Color,
  val background: Color,
  val secondaryBackground: Color,
  val bottomBarBackground: Color,
  val cardBackground: Color,
  val secondaryContent: Color,
  val cardCaption: Color = Color(0xFFBAC9DF),
  val cardCaption60: Color = Color(0x99BAC9DF),
  val cardMenu: Color = Color(0xFFBAC9DF),
  val cardAction: Color = Color(0xFF7E8C9F),
  val cardLike: Color = Color(0xFFEF7096),
  val replyLine: Color,
  val hintText: Color,
  val reblogged: Color = Color(0xFF18BE64),
  val replyTextFieldBorder: Color,
  val followButtonBackground: Color = accent,
  val unfollowButtonBackground: Color = Color(69, 69, 69),
  val defaultHeader: Color,
  val divider: Color,
  val bottomSheetBackground: Color,
  val bottomSheetItemBackground: Color,
  val bottomSheetItemSelectedIcon: Color,
  val bottomSheetItemSelectedBackground: Color,
  val bottomSheetSelectedBorder: Color,
  val exploreSearchBarBorder: Color,
  val exploreSearchBarBackground: Color,
  val pollOptionBackground: Color,
  val textLimitWarningBackground: Color,
  val dialogItemSelectedBackground: Color = Color(0xFF5691E1).copy(alpha = 0.8f),
  val dialogItemUnselectedBackground: Color,
  val dialogItemUnselectedContent: Color,
  val searchPreviewBorder: Color,
  val searchPreviewBackground: Color,
  val isLight: Boolean
) {
  val isDark: Boolean inline get() = !isLight

  companion object {
    val Light = MastifyColorScheme(
      primaryContent = Color(0xFF081B34),
      primaryGradient = Brush.linearGradient(listOf(Color(0xFF143D73), Color(0xFF081B34))),
      accent = Color(0xFF046FFF),
      accent10 = Color(0xE6046FFF).copy(alpha = 0.1f),
      background = Color.White,
      secondaryBackground = Color(0xFFF5F5F5),
      bottomBarBackground = Color.White,
      cardBackground = Color.White,
      secondaryContent = Color(0xFF7489A6),
      replyLine = Color(0xFFcfd9de),
      hintText = Color(0xFF1d9bf0),
      replyTextFieldBorder = Color(0xFFE6E6E6),
      defaultHeader = Color(0xFF1d9bf0),
      divider = Color(0xFFD7D7D7).copy(0.5f),
      bottomSheetBackground = Color.White,
      bottomSheetItemBackground = Color(0xFFE2E4E9).copy(0.4f),
      bottomSheetItemSelectedIcon = Color(0xFF1E72E2),
      bottomSheetItemSelectedBackground = Color(0xFFE2E4E9).copy(0.4f),
      bottomSheetSelectedBorder = Color(0xFFAAC8F5),
      exploreSearchBarBorder = Color(0xFFF1F1F1),
      exploreSearchBarBackground = Color.White,
      pollOptionBackground = Color(0xFFECEEF0),
      textLimitWarningBackground = Color(0xFFf8d3de),
      dialogItemUnselectedBackground = Color(0xFF203148).copy(alpha = 0.06f),
      dialogItemUnselectedContent = Color(0xFF969696),
      searchPreviewBorder = Color(0xFFD7D7D7).copy(.23f),
      searchPreviewBackground = Color(0xFFFAFAFA),
      isLight = true
    )

    val Dark = MastifyColorScheme(
      primaryContent = Color.White,
      primaryGradient = Brush.linearGradient(listOf(Color(0xFF143D73), Color(0xFF081B34))),
      accent = Color(0xFF046FFF),
      accent10 = Color(0xE6046FFF).copy(alpha = 0.1f),
      background = Color(0xFF141417),
      secondaryBackground = Color(0x0FFFFFFF),
      bottomBarBackground = Color(0xFF242424),
      cardBackground = Color(0x0FFFFFFF),
      secondaryContent = Color(0xFF7489A6),
      replyLine = Color(0xFF333638),
      hintText = Color(0xFF1d9bf0),
      replyTextFieldBorder = Color(0xFF454545),
      defaultHeader = Color(0xFF1d9bf0),
      divider = Color(0xFFD7D7D7).copy(0.1f),
      bottomSheetBackground = Color(0xFF31323A),
      bottomSheetItemBackground = Color(0xFF24252B).copy(0.6f),
      bottomSheetItemSelectedIcon = Color.White,
      bottomSheetItemSelectedBackground = Color(0xFF4E5059).copy(0.4f),
      bottomSheetSelectedBorder = Color.White.copy(0.2f),
      exploreSearchBarBorder = Color(0xFF222222),
      exploreSearchBarBackground = Color(0xFF1D1D1D),
      pollOptionBackground = Color(0xFF232323),
      textLimitWarningBackground = Color(0xFFf3222d),
      dialogItemUnselectedBackground = Color(0xFFC3DDFF).copy(alpha = 0.38f),
      dialogItemUnselectedContent = Color(0xFFEEEEEE),
      searchPreviewBorder = Color(0xFF3C3C3C),
      searchPreviewBackground = Color(0xFF232323),
      isLight = false
    )
  }
}

val LocalColorScheme = staticCompositionLocalOf<MastifyColorScheme> { noCompositionLocalProvided() }

val currentColorScheme: MastifyColorScheme
  @Composable
  @ReadOnlyComposable
  get() = LocalColorScheme.current

/**
 * Creates a color based on the current color scheme (light or dark).
 *
 * @param light The color to use when the current color scheme is light.
 * @param dark The color to use when the current color scheme is dark.
 * @return The appropriate color based on the current color scheme.
 */
@Composable
@ReadOnlyComposable
fun color(light: Color, dark: Color): Color = when (LocalColorScheme.current.isLight) {
  true -> light
  false -> dark
}
