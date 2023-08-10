package com.github.whitescent.mastify.ui.theme

import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Stable
interface MastifyColorsInterface {
  val primaryContent: Color
  val primaryGradient: Brush
  val accent: Color
  val accent10: Color
  val background: Color
  val secondaryBackground: Color
  val bottomBarBackground: Color
  val cardBackground: Color
  val secondaryContent: Color
  val cardCaption: Color
    get() = Color(0xFFBAC9DF)
  val cardCaption60: Color
    get() = Color(0x99BAC9DF)
  val cardMenu: Color
    get() = Color(0xFFBAC9DF)
  val cardAction: Color
    get() = Color(0xFF7E8C9F)
  val cardLike: Color
    get() = Color(0xFFEF7096)
  val replyLine: Color
  val hintText: Color
    get() = Color(0xFF1d9bf0)
  val replyTextFieldBackground: Color
  val replyTextFieldBorder: Color
  val followButton: Color
  val unfollowButton: Color
  val defaultHeader: Color
  val divider: Color
}

private object LightColors : MastifyColorsInterface {
  override val primaryContent: Color
    get() = Color(0xFF081B34)

  override val primaryGradient: Brush
    get() = Brush.linearGradient(listOf(Color(0xFF143D73), Color(0xFF081B34)))

  override val accent: Color
    get() = Color(0xFF046FFF)

  override val accent10: Color
    get() = Color(0xE6046FFF).copy(alpha = 0.1f)

  override val background: Color
    get() = Color.White

  override val secondaryBackground: Color
    get() = Color(0xFFFFFFFF)

  override val bottomBarBackground: Color
    get() = secondaryBackground

  override val cardBackground: Color
    get() = secondaryBackground

  override val secondaryContent: Color
    get() = Color(0xFF7489A6)

  override val replyLine: Color
    get() = Color(0xFFcfd9de)

  override val replyTextFieldBackground: Color
    get() = Color(0xFFF4F4F4)

  override val replyTextFieldBorder: Color
    get() = Color(0xFFE6E6E6)

  override val followButton: Color
    get() = accent

  override val unfollowButton: Color
    get() = Color.White

  override val defaultHeader: Color
    get() = accent10

  override val divider: Color
    get() = Color(0xFFD7D7D7).copy(0.5f)
}

private object DarkColors : MastifyColorsInterface {
  override val primaryContent: Color
    get() = Color.White

  override val primaryGradient: Brush
    get() = Brush.linearGradient(listOf(Color(0xFF143D73), Color(0xFF081B34)))

  override val accent: Color
    get() = Color(0xFF046FFF)

  override val accent10: Color
    get() = Color(0xE6046FFF)

  override val background: Color
    get() = Color(0xFF141417)

  override val secondaryBackground: Color
    get() = Color.Black

  override val bottomBarBackground: Color
    get() = Color(0xFF242424)

  override val cardBackground: Color
    get() = Color(0x0FFFFFFF)

  override val secondaryContent: Color
    get() = Color(0xFF7489A6)

  override val replyLine: Color
    get() = Color(0xFF333638)

  override val replyTextFieldBackground: Color
    get() = Color(0xFF282828)

  override val replyTextFieldBorder: Color
    get() = Color(0xFF454545)

  override val followButton: Color
    get() = Color.White

  override val unfollowButton: Color
    get() = Color.Black

  override val defaultHeader: Color
    get() = Color(0xFF1f9ff1)

  override val divider: Color
    get() = Color(0xFFD7D7D7).copy(0.1f)
}

class MastifyColors : MastifyColorsInterface {

  var isLight by mutableStateOf(true)
    private set

  private val currentColors by derivedStateOf {
    if (isLight) LightColors else DarkColors
  }

  fun toggleTheme() { isLight = !isLight }
  fun toggleToLightColor() { isLight = true }
  fun toggleToDarkColor() { isLight = false }

  override val primaryContent: Color
    get() = currentColors.primaryContent
  override val primaryGradient: Brush
    get() = currentColors.primaryGradient
  override val accent: Color
    get() = currentColors.accent
  override val accent10: Color
    get() = currentColors.accent10
  override val background: Color
    get() = currentColors.background
  override val secondaryBackground: Color
    get() = currentColors.secondaryBackground
  override val bottomBarBackground: Color
    get() = currentColors.bottomBarBackground
  override val cardBackground: Color
    get() = currentColors.cardBackground
  override val secondaryContent: Color
    get() = currentColors.secondaryContent
  override val replyLine: Color
    get() = currentColors.replyLine
  override val replyTextFieldBackground: Color
    get() = currentColors.replyTextFieldBackground
  override val replyTextFieldBorder: Color
    get() = currentColors.replyTextFieldBorder
  override val followButton: Color
    get() = currentColors.followButton
  override val unfollowButton: Color
    get() = currentColors.unfollowButton
  override val defaultHeader: Color
    get() = currentColors.defaultHeader
  override val divider: Color
    get() = currentColors.divider
}

val LocalMastifyColors = staticCompositionLocalOf {
  MastifyColors()
}
