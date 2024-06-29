package com.github.whitescent.mastify.core.common.compose

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier

@Stable
fun Modifier.clickableWithoutRipple(
  enabled: Boolean = true,
  onClick: () -> Unit
) = if (enabled) clickable(
  onClick = onClick,
  indication = null,
  interactionSource = null,
) else Modifier

/**
 * Applies the given [modifier] if the [condition] is `true`.
 *
 * @param condition The condition to check.
 * @param modifier The modifier to apply if the [condition] is `true`.
 */
inline fun Modifier.thenIf(
  condition: Boolean,
  modifier: Modifier.() -> Modifier,
): Modifier = if (condition) modifier() else this
