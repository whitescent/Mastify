package com.github.whitescent.mastify.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable

object AppTheme {

  val colors: MastifyColorScheme
    @Composable
    @ReadOnlyComposable
    get() = if (isSystemInDarkTheme()) DarkColorScheme else LightColorScheme

  val typography: MastifyTypography
    @Composable
    @ReadOnlyComposable
    get() = LocalMastifyTypography.current

  val shape: MastifyShape
    @Composable
    @ReadOnlyComposable
    get() = LocalMastifyShape.current
}
