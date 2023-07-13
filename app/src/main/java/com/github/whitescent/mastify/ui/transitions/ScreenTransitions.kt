package com.github.whitescent.mastify.ui.transitions

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.NavBackStackEntry
import com.github.whitescent.mastify.screen.appDestination
import com.github.whitescent.mastify.screen.destinations.HomeDestination
import com.github.whitescent.mastify.screen.destinations.LoginDestination
import com.github.whitescent.mastify.screen.destinations.OauthDestination
import com.github.whitescent.mastify.screen.destinations.StatusDetailDestination
import com.ramcosta.composedestinations.spec.DestinationStyle

object AppTransitions : DestinationStyle.Animated {
  override fun AnimatedContentTransitionScope<NavBackStackEntry>.enterTransition(): EnterTransition {
    return when (initialState.appDestination()) {
      HomeDestination -> scaleIn(tween(500), initialScale = 0.5f) + fadeIn()
      StatusDetailDestination, LoginDestination -> {
        slideInHorizontally(
          initialOffsetX = { -slideAnimationOffset },
          animationSpec = tween(slideAnimationTween)
        )
      }
      else -> fadeIn()
    }
  }
  override fun AnimatedContentTransitionScope<NavBackStackEntry>.exitTransition(): ExitTransition {
    return when (targetState.appDestination()) {
      HomeDestination -> scaleOut(tween(500), targetScale = 0.5f) + fadeOut()
      StatusDetailDestination, LoginDestination, OauthDestination -> {
        slideOutHorizontally(
          targetOffsetX = { -slideAnimationOffset },
          animationSpec = tween(slideAnimationTween)
        )
      }
      else -> fadeOut()
    }
  }
}

object LoginTransitions : DestinationStyle.Animated {
  override fun AnimatedContentTransitionScope<NavBackStackEntry>.enterTransition(): EnterTransition {
    return when (initialState.appDestination()) {
      HomeDestination ->
        slideInHorizontally(
          initialOffsetX = { slideAnimationOffset },
          animationSpec = tween(slideAnimationTween)
        )
      OauthDestination -> scaleIn(tween(500), initialScale = 0.5f) + fadeIn()
      else -> fadeIn()
    }
  }
  override fun AnimatedContentTransitionScope<NavBackStackEntry>.exitTransition(): ExitTransition {
    return when (targetState.appDestination()) {
      HomeDestination ->
        slideOutHorizontally(
          targetOffsetX = { slideAnimationOffset },
          animationSpec = tween(slideAnimationTween)
        )
      else -> fadeOut()
    }
  }
}

object OauthTransitions : DestinationStyle.Animated {
  override fun AnimatedContentTransitionScope<NavBackStackEntry>.enterTransition(): EnterTransition {
    return fadeIn()
  }
  override fun AnimatedContentTransitionScope<NavBackStackEntry>.exitTransition(): ExitTransition {
    return when (targetState.appDestination()) {
      HomeDestination -> scaleOut(tween(500), targetScale = 0.5f) + fadeOut()
      else -> fadeOut()
    }
  }
}

object StatusTransitions : DestinationStyle.Animated {
  override fun AnimatedContentTransitionScope<NavBackStackEntry>.enterTransition(): EnterTransition {
    return when (initialState.appDestination()) {
      HomeDestination ->
        slideInHorizontally(
          initialOffsetX = { slideAnimationOffset },
          animationSpec = tween(slideAnimationTween)
        )
      else -> fadeIn()
    }
  }

  override fun AnimatedContentTransitionScope<NavBackStackEntry>.exitTransition(): ExitTransition {
    return when (targetState.appDestination()) {
      HomeDestination ->
        slideOutHorizontally(
          targetOffsetX = { slideAnimationOffset },
          animationSpec = tween(slideAnimationTween)
        )
      else -> fadeOut()
    }
  }
}

object StatusMediaTransitions : DestinationStyle.Animated {
  override fun AnimatedContentTransitionScope<NavBackStackEntry>.enterTransition(): EnterTransition {
    return scaleIn(tween(200, easing = LinearEasing)) + fadeIn()
  }

  override fun AnimatedContentTransitionScope<NavBackStackEntry>.exitTransition(): ExitTransition {
    return scaleOut(tween(200, easing = LinearEasing)) + fadeOut()
  }
}

const val slideAnimationOffset = 1200
const val slideAnimationTween = 300
