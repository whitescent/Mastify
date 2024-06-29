package com.github.whitescent.mastify.core.common.compose

import android.content.Context
import android.content.res.Configuration
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

inline fun <reified T> noCompositionLocalProvided(): T {
  error("CompositionLocal ${T::class.java.simpleName} not present")
}

/**
 * A convenient way to provide a value conditionally.
 *
 * @param condition the condition to determine whether to provide the value.
 * @param value the value to be provided.
 * @return the provided value if the condition is `true`, otherwise `null`.
 *
 * @see ProvidableCompositionLocal.provides
 */
inline fun <T> ProvidableCompositionLocal<T>.providesIf(
  condition: Boolean,
  value: () -> T,
): ProvidedValue<T>? = if (condition) provides(value()) else null

/**
 * A convenient way to access the [View] instance in the current hierarchy.
 * This is an alias for `LocalView.current`.
 */
val currentView: View
  @Composable
  @ReadOnlyComposable
  get() = LocalView.current

/**
 * A convenient way to access the [Context] instance in the current hierarchy.
 * This is an alias for `LocalContext.current`.
 */
val currentContext: Context
  @Composable
  @ReadOnlyComposable
  get() = LocalContext.current

/**
 * A convenient way to access the [Configuration] instance in the current hierarchy.
 * This is an alias for `LocalConfiguration.current`.
 */
val currentConfiguration: Configuration
  @Composable
  @ReadOnlyComposable
  get() = LocalConfiguration.current

/**
 * The height of the available screen space in dp units excluding the area
 * occupied by {@link android.view.WindowInsets window insets}, such as the
 * status bar, navigation bar, and cutouts.
 *
 */
val currentScreenHeightDp: Dp
  @Composable
  @ReadOnlyComposable
  get() = currentConfiguration.screenHeightDp.dp

/**
 * A convenient way to check whether the current environment is in edit mode.
 *
 * @see View.isInEditMode
 */
val currentIsInEditMode: Boolean
  @Composable
  @ReadOnlyComposable
  get() = currentView.isInEditMode
