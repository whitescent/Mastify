package com.github.whitescent.mastify.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable

object AppTheme {

  val colors: MastifyColorsInterface
    @Composable
    get() = LocalMastifyColors.current

  val typography: MastifyTypography
    @Composable
    @ReadOnlyComposable
    get() = LocalMastifyTypography.current
}
