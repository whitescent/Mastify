package com.github.whitescent.mastify.ui.theme

import androidx.compose.ui.text.font.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

class MastifyTypography {
  
  val statusDisplayName @Composable
    get() = TextStyle(
      fontSize = 15.sp,
      fontWeight = FontWeight(550),
      color = AppTheme.colors.primaryContent
    )
  
  val statusUsername @Composable
    get() = TextStyle(
      fontSize = 12.sp,
      color = AppTheme.colors.primaryContent.copy(alpha = 0.5f)
    )
  
  val statusRepost @Composable
    get() = TextStyle(
      fontSize = 14.sp,
      fontWeight = FontWeight.Normal,
      color = AppTheme.colors.primaryContent
    )
  
  val statusActions @Composable
    get() = TextStyle(
      fontSize = 12.sp,
      color = AppTheme.colors.cardAction
    )
  
}

val LocalMastifyTypography = staticCompositionLocalOf {
  MastifyTypography()
}
