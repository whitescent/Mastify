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

package com.github.whitescent.mastify.ui.transitions

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Companion.End
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Companion.Start
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.navigation.NavBackStackEntry
import com.github.whitescent.mastify.utils.isBottomBarScreen
import com.ramcosta.composedestinations.animations.NavHostAnimatedDestinationStyle
import com.ramcosta.composedestinations.generated.destinations.StatusMediaScreenDestination
import com.ramcosta.composedestinations.spec.DestinationStyle
import com.ramcosta.composedestinations.utils.destination

private const val slideAnimationTween = 300
private const val scaleSize = 0.75f

object DefaultAppTransitions : NavHostAnimatedDestinationStyle() {
  override val enterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition
     = { defaultSlideIntoContainer() }
  override val exitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition
    = { defaultSlideOutContainer() }
  override val popEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition
    = {  defaultSlideIntoContainer(forward = false) }
  override val popExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition
    = { defaultSlideOutContainer(forward = false) }
}

fun <T> AnimatedContentTransitionScope <T>.defaultSlideIntoContainer(
  forward: Boolean = true
): EnterTransition {
  return when (forward) {
    true -> slideIntoContainer(Start, tween(slideAnimationTween, easing = FastOutSlowInEasing))
    false -> scaleIn(initialScale = scaleSize, animationSpec = tween(300, easing = EaseOutCubic)) +
      fadeIn(animationSpec = tween(300, delayMillis = 80), initialAlpha = 0.15f)
  }
}

fun <T> AnimatedContentTransitionScope<T>.defaultSlideOutContainer(
  forward: Boolean = true
): ExitTransition = when (forward) {
  true -> scaleOut(targetScale = scaleSize, animationSpec = tween(400, easing = EaseInOutCubic)) +
    fadeOut(targetAlpha = 0.15f)
  false -> slideOutOfContainer(End, tween(slideAnimationTween))
}

object BottomBarScreenTransitions : DestinationStyle.Animated() {
  override val enterTransition:
    AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition? = {
    if (initialState.destination == targetState.destination) {
      defaultSlideIntoContainer() // transition when changing account
    } else {
      when (initialState.destination().isBottomBarScreen) {
        true -> EnterTransition.None
        else -> defaultSlideIntoContainer()
      }
    }
  }
  override val exitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?
    = {
      if (initialState.destination == targetState.destination) {
      defaultSlideOutContainer()
    } else {
      when (targetState.destination().isBottomBarScreen) {
        true -> ExitTransition.None
        else -> defaultSlideOutContainer()
      }
    }
  }
  override val popEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?
    = {
      when (initialState.destination().isBottomBarScreen) {
      true -> EnterTransition.None
      else -> defaultSlideIntoContainer(forward = false)
    }
  }
  override val popExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?
    = {
    when (targetState.destination().isBottomBarScreen) {
      true -> ExitTransition.None
      else -> defaultSlideOutContainer(forward = false)
    }
  }
}
