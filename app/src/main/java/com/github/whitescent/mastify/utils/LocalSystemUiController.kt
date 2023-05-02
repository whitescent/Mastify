package com.github.whitescent.mastify.utils

import androidx.compose.runtime.compositionLocalOf
import com.google.accompanist.systemuicontroller.SystemUiController

val LocalSystemUiController = compositionLocalOf<SystemUiController> {
  error("CompositionLocal LocalNavController not present")
}