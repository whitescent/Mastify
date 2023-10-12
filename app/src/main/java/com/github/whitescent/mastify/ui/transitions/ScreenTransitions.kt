/*
 * Copyright 2023 WhiteScent
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
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation.NavBackStackEntry
import com.github.whitescent.mastify.screen.appDestination
import com.github.whitescent.mastify.utils.isBottomBarScreen
import com.ramcosta.composedestinations.spec.DestinationStyle

object BottomBarScreenTransitions : DestinationStyle.Animated {
  override fun AnimatedContentTransitionScope<NavBackStackEntry>.enterTransition(): EnterTransition {
    return when (initialState.appDestination().isBottomBarScreen) {
      true -> fadeIn()
      else -> slideIntoContainer(
        towards = Start,
        animationSpec = tween(slideAnimationTween)
      )
    }
  }
  override fun AnimatedContentTransitionScope<NavBackStackEntry>.exitTransition(): ExitTransition {
    return when (targetState.appDestination().isBottomBarScreen) {
      true -> fadeOut()
      else -> slideOutOfContainer(
        towards = Start,
        animationSpec = tween(slideAnimationTween)
      )
    }
  }
  override fun AnimatedContentTransitionScope<NavBackStackEntry>.popEnterTransition(): EnterTransition {
    return when (initialState.appDestination().isBottomBarScreen) {
      true -> fadeIn()
      else -> slideIntoContainer(
        towards = End,
        animationSpec = tween(slideAnimationTween)
      )
    }
  }
  override fun AnimatedContentTransitionScope<NavBackStackEntry>.popExitTransition(): ExitTransition {
    return when (targetState.appDestination().isBottomBarScreen) {
      true -> fadeOut()
      else -> slideOutOfContainer(
        towards = End,
        animationSpec = tween(slideAnimationTween)
      )
    }
  }
}

const val slideAnimationTween = 300
