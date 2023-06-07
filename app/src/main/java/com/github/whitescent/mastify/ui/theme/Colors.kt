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
}

private object LightColors: MastifyColorsInterface {
  override val primaryContent: Color
    get() = Color(0xFF081B34)

  override val primaryGradient: Brush
    get() = Brush.linearGradient(listOf(Color(0xFF143D73), Color(0xFF081B34)))

  override val accent: Color
    get() = Color(0xFF046FFF)

  override val accent10: Color
    get() = Color(0xE6046FFF)

  override val background: Color
    get() = Color(0xFFF8F8FB)

  override val secondaryBackground: Color
    get() = Color(0xFFFFFFFF)

  override val bottomBarBackground: Color
    get() = secondaryBackground

  override val cardBackground: Color
    get() = secondaryBackground

  override val secondaryContent: Color
    get() = Color(0xFF7489A6)
}

private object DarkColors: MastifyColorsInterface {
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
    get() = secondaryBackground

  override val cardBackground: Color
    get() = Color(0x0FFFFFFF)

  override val secondaryContent: Color
    get() = Color(0xFF7489A6)
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
}

val LocalMastifyColors = staticCompositionLocalOf {
  MastifyColors()
}
