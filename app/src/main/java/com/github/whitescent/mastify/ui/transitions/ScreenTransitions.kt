package com.github.whitescent.mastify.ui.transitions

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.NavBackStackEntry
import com.github.whitescent.mastify.screen.appDestination
import com.github.whitescent.mastify.screen.destinations.DirectMessageDestination
import com.github.whitescent.mastify.screen.destinations.ExplorerDestination
import com.github.whitescent.mastify.screen.destinations.HomeDestination
import com.github.whitescent.mastify.screen.destinations.LoginDestination
import com.github.whitescent.mastify.screen.destinations.NotificationDestination
import com.github.whitescent.mastify.screen.destinations.OauthDestination
import com.github.whitescent.mastify.screen.destinations.StatusDetailDestination
import com.ramcosta.composedestinations.spec.DestinationStyle

object AppTransitions : DestinationStyle.Animated {

  override fun AnimatedContentTransitionScope<NavBackStackEntry>.enterTransition(): EnterTransition? {
    return when (initialState.appDestination()) {
      HomeDestination -> scaleIn(tween(500), initialScale = 0.5f) + fadeIn()
      ExplorerDestination -> fadeIn()
      NotificationDestination -> fadeIn()
      DirectMessageDestination -> fadeIn()
      StatusDetailDestination, LoginDestination -> {
        slideInHorizontally(
          initialOffsetX = { -slideAnimationOffset },
          animationSpec = tween(slideAnimationTween)
        )
      }
      else -> null
    }
  }
  override fun AnimatedContentTransitionScope<NavBackStackEntry>.exitTransition(): ExitTransition {
    return when (targetState.appDestination()) {
      HomeDestination -> scaleOut(tween(500), targetScale = 0.5f) + fadeOut()
      ExplorerDestination -> fadeOut()
      NotificationDestination -> fadeOut()
      DirectMessageDestination -> fadeOut()
      StatusDetailDestination, LoginDestination, OauthDestination -> {
        slideOutHorizontally(
          targetOffsetX = { -slideAnimationOffset },
          animationSpec = tween(slideAnimationTween)
        )
      }
    }
  }
}

object LoginTransitions : DestinationStyle.Animated {
  override fun AnimatedContentTransitionScope<NavBackStackEntry>.enterTransition(): EnterTransition? {
    return when (initialState.appDestination()) {
      HomeDestination ->
        slideInHorizontally(
          initialOffsetX = { slideAnimationOffset },
          animationSpec = tween(slideAnimationTween)
        )
      OauthDestination -> scaleIn(tween(500), initialScale = 0.5f) + fadeIn()
      else -> null
    }
  }
  override fun AnimatedContentTransitionScope<NavBackStackEntry>.exitTransition(): ExitTransition? {
    return when (targetState.appDestination()) {
      HomeDestination ->
        slideOutHorizontally(
          targetOffsetX = { slideAnimationOffset },
          animationSpec = tween(slideAnimationTween)
        )
      else -> null
    }
  }
}

object OauthTransitions : DestinationStyle.Animated {
  override fun AnimatedContentTransitionScope<NavBackStackEntry>.enterTransition(): EnterTransition {
    return fadeIn()
  }
  override fun AnimatedContentTransitionScope<NavBackStackEntry>.exitTransition(): ExitTransition? {
    return when (targetState.appDestination()) {
      LoginDestination -> fadeOut()
      HomeDestination -> scaleOut(tween(500), targetScale = 0.5f) + fadeOut()
      else -> null
    }
  }
}

object StatusTransitions : DestinationStyle.Animated {
  override fun AnimatedContentTransitionScope<NavBackStackEntry>.enterTransition(): EnterTransition? {
    return when (initialState.appDestination()) {
      HomeDestination ->
        slideInHorizontally(
          initialOffsetX = { slideAnimationOffset },
          animationSpec = tween(slideAnimationTween)
        )
      else -> null
    }
  }

  override fun AnimatedContentTransitionScope<NavBackStackEntry>.exitTransition(): ExitTransition? {
    return when (targetState.appDestination()) {
      HomeDestination ->
        slideOutHorizontally(
          targetOffsetX = { slideAnimationOffset },
          animationSpec = tween(slideAnimationTween)
        )
      else -> null
    }
  }
}

const val slideAnimationOffset = 1000
const val slideAnimationTween = 300
