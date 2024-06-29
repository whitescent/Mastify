package com.github.whitescent.mastify.core.common.compose

import androidx.activity.ComponentActivity
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Provides a `CompositionLocal` for accessing the [ComponentActivity] instance.
 *
 * If no [ComponentActivity] can be found, the default value will be `null`.
 */
val LocalActivity = staticCompositionLocalOf<ComponentActivity?> { noCompositionLocalProvided() }
