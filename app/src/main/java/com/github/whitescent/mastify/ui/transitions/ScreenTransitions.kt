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
import com.github.whitescent.mastify.screen.appDestination
import com.github.whitescent.mastify.screen.destinations.StatusMediaScreenDestination
import com.github.whitescent.mastify.utils.isBottomBarScreen
import com.ramcosta.composedestinations.spec.DestinationStyle

private const val slideAnimationTween = 300
private const val scaleSize = 0.75f

fun AnimatedContentTransitionScope<NavBackStackEntry>.defaultSlideIntoContainer(
  forward: Boolean = true
): EnterTransition {
  return if (targetState.appDestination() == StatusMediaScreenDestination) EnterTransition.None
  else when (forward) {
    true -> slideIntoContainer(Start, tween(slideAnimationTween, easing = FastOutSlowInEasing))
    false -> scaleIn(initialScale = scaleSize, animationSpec = tween(300, easing = EaseOutCubic)) +
      fadeIn(animationSpec = tween(300, delayMillis = 80), initialAlpha = 0.15f)
  }
}

fun AnimatedContentTransitionScope<NavBackStackEntry>.defaultSlideOutContainer(
  forward: Boolean = true
): ExitTransition = when (forward) {
  true -> scaleOut(targetScale = scaleSize, animationSpec = tween(400, easing = EaseInOutCubic)) +
    fadeOut(targetAlpha = 0.15f)
  false -> slideOutOfContainer(End, tween(slideAnimationTween))
}

object BottomBarScreenTransitions : DestinationStyle.Animated {
  override fun AnimatedContentTransitionScope<NavBackStackEntry>.enterTransition(): EnterTransition {
    return if (initialState.destination == targetState.destination) {
      defaultSlideIntoContainer() // transition when changing account
    } else {
      when (initialState.appDestination().isBottomBarScreen) {
        true -> EnterTransition.None
        else -> defaultSlideIntoContainer()
      }
    }
  }
  override fun AnimatedContentTransitionScope<NavBackStackEntry>.exitTransition(): ExitTransition {
    return if (initialState.destination == targetState.destination) {
      defaultSlideOutContainer()
    } else {
      when (targetState.appDestination().isBottomBarScreen || initialState.appDestination() == StatusMediaScreenDestination) {
        true -> ExitTransition.None
        else -> defaultSlideOutContainer()
      }
    }
  }
  override fun AnimatedContentTransitionScope<NavBackStackEntry>.popEnterTransition(): EnterTransition {
    return when (initialState.appDestination().isBottomBarScreen) {
      true -> EnterTransition.None
      else -> defaultSlideIntoContainer(forward = false)
    }
  }
  override fun AnimatedContentTransitionScope<NavBackStackEntry>.popExitTransition(): ExitTransition {
    return when (targetState.appDestination().isBottomBarScreen) {
      true -> ExitTransition.None
      else -> defaultSlideOutContainer(forward = false)
    }
  }
}
