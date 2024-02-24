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

package com.github.whitescent.mastify.ui.theme

import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Stable
interface MastifyColorScheme {
  val primaryContent: Color
  val primaryGradient: Brush
  val accent: Color
  val accent10: Color
  val background: Color
  val secondaryBackground: Color
  val bottomBarBackground: Color
  val cardBackground: Color
  val secondaryContent: Color
  val cardCaption: Color get() = Color(0xFFBAC9DF)
  val cardCaption60: Color get() = Color(0x99BAC9DF)
  val cardMenu: Color get() = Color(0xFFBAC9DF)
  val cardAction: Color get() = Color(0xFF7E8C9F)
  val cardLike: Color get() = Color(0xFFEF7096)
  val replyLine: Color
  val hintText: Color
  val reblogged: Color get() = Color(0xFF18BE64)
  val replyTextFieldBorder: Color
  val followButtonBackground: Color get() = accent
  val unfollowButtonBackground: Color get() = Color(69, 69, 69)
  val defaultHeader: Color
  val divider: Color
  val bottomSheetBackground: Color
  val bottomSheetItemBackground: Color
  val bottomSheetItemSelectedIcon: Color
  val bottomSheetItemSelectedBackground: Color
  val bottomSheetSelectedBorder: Color
  val exploreSearchBarBorder: Color
  val exploreSearchBarBackground: Color
  val pollOptionBackground: Color
  val textLimitWarningBackground: Color
  val dialogItemSelectedBackground: Color get() = Color(0xFF5691E1).copy(alpha = 0.8f)
  val dialogItemUnselectedBackground: Color
  val dialogItemUnselectedContent: Color
  val searchPreviewBorder: Color
  val searchPreviewBackground: Color
  val isLight: Boolean
}

object LightColorScheme : MastifyColorScheme {
  override val primaryContent: Color = Color(0xFF081B34)
  override val primaryGradient: Brush = Brush.linearGradient(listOf(Color(0xFF143D73), Color(0xFF081B34)))
  override val accent: Color = Color(0xFF046FFF)
  override val accent10: Color = Color(0xE6046FFF).copy(alpha = 0.1f)
  override val background: Color = Color.White
  override val secondaryBackground: Color = Color.White
  override val bottomBarBackground: Color = Color.White
  override val cardBackground: Color = Color.White
  override val secondaryContent: Color = Color(0xFF7489A6)
  override val replyLine: Color = Color(0xFFcfd9de)
  override val hintText: Color = Color(0xFF1d9bf0)
  override val replyTextFieldBorder: Color = Color(0xFFE6E6E6)
  override val defaultHeader: Color = Color(0xFF1d9bf0)
  override val divider: Color = Color(0xFFD7D7D7).copy(0.5f)
  override val bottomSheetBackground: Color = Color.White
  override val bottomSheetItemBackground: Color = Color(0xFFE2E4E9).copy(0.4f)
  override val bottomSheetItemSelectedIcon: Color = Color(0xFF1E72E2)
  override val bottomSheetItemSelectedBackground: Color = Color(0xFFE2E4E9).copy(0.4f)
  override val bottomSheetSelectedBorder: Color = Color(0xFFAAC8F5)
  override val exploreSearchBarBorder: Color = Color(0xFFF1F1F1)
  override val exploreSearchBarBackground: Color = Color.White
  override val pollOptionBackground: Color = Color(0xFFECEEF0)
  override val textLimitWarningBackground: Color = Color(0xFFf8d3de)
  override val dialogItemUnselectedBackground: Color = Color(0xFF203148).copy(alpha = 0.06f)
  override val dialogItemUnselectedContent: Color = Color(0xFF969696)
  override val searchPreviewBorder: Color = Color(0xFFD7D7D7).copy(.23f)
  override val searchPreviewBackground: Color = Color(0xFFFAFAFA)
  override val isLight: Boolean = true
}

object DarkColorScheme : MastifyColorScheme {
  override val primaryContent = Color.White
  override val primaryGradient = Brush.linearGradient(listOf(Color(0xFF143D73), Color(0xFF081B34)))
  override val accent = Color(0xFF046FFF)
  override val accent10 = Color(0xE6046FFF).copy(alpha = 0.1f)
  override val background = Color(0xFF141417)
  override val secondaryBackground = Color.Black
  override val bottomBarBackground = Color(0xFF242424)
  override val cardBackground = Color(0x0FFFFFFF)
  override val secondaryContent = Color(0xFF7489A6)
  override val replyLine = Color(0xFF333638)
  override val hintText = Color(0xFF1d9bf0)
  override val replyTextFieldBorder = Color(0xFF454545)
  override val defaultHeader = Color(0xFF1d9bf0)
  override val divider = Color(0xFFD7D7D7).copy(0.1f)
  override val bottomSheetBackground = Color(0xFF31323A)
  override val bottomSheetItemBackground = Color(0xFF24252B).copy(0.6f)
  override val bottomSheetItemSelectedIcon = Color.White
  override val bottomSheetItemSelectedBackground = Color(0xFF4E5059).copy(0.4f)
  override val bottomSheetSelectedBorder = Color.White.copy(0.2f)
  override val exploreSearchBarBorder = Color(0xFF222222)
  override val exploreSearchBarBackground = Color(0xFF1D1D1D)
  override val pollOptionBackground = Color(0xFF232323)
  override val textLimitWarningBackground = Color(0xFFf3222d)
  override val dialogItemUnselectedBackground: Color = Color(0xFFC3DDFF).copy(alpha = 0.38f)
  override val dialogItemUnselectedContent: Color = Color(0xFFEEEEEE)
  override val searchPreviewBorder: Color = Color(0xFF3C3C3C)
  override val searchPreviewBackground: Color = Color(0xFF232323)
  override val isLight = false
}

val LocalMastifyColors = staticCompositionLocalOf<MastifyColorScheme> { LightColorScheme }
