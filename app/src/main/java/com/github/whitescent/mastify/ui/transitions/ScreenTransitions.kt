package com.github.whitescent.mastify.ui.transitions

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Companion.End
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Companion.Start
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.navigation.NavBackStackEntry
import com.github.whitescent.mastify.screen.appDestination
import com.github.whitescent.mastify.screen.destinations.HomeDestination
import com.github.whitescent.mastify.screen.destinations.LoginDestination
import com.github.whitescent.mastify.screen.destinations.OauthDestination
import com.github.whitescent.mastify.screen.destinations.ProfileDestination
import com.github.whitescent.mastify.screen.destinations.StatusDetailDestination
import com.github.whitescent.mastify.screen.destinations.StatusMediaScreenDestination
import com.ramcosta.composedestinations.spec.DestinationStyle

object AppTransitions : DestinationStyle.Animated {
  override fun AnimatedContentTransitionScope<NavBackStackEntry>.enterTransition(): EnterTransition {
    return when (initialState.appDestination()) {
      HomeDestination -> scaleIn(tween(500), initialScale = 0.5f) + fadeIn()
      LoginDestination, StatusDetailDestination, ProfileDestination -> {
        slideIntoContainer(
          towards = End,
          animationSpec = tween(slideAnimationTween)
        ) + fadeIn()
      }
      else -> fadeIn()
    }
  }
  override fun AnimatedContentTransitionScope<NavBackStackEntry>.exitTransition(): ExitTransition? {
    return when (targetState.appDestination()) {
      HomeDestination -> scaleOut(tween(500), targetScale = 0.5f) + fadeOut()
      StatusDetailDestination, LoginDestination, OauthDestination, ProfileDestination -> {
        slideOutOfContainer(
          towards = Start,
          animationSpec = tween(slideAnimationTween)
        ) + fadeOut()
      }
      StatusMediaScreenDestination -> null
      else -> fadeOut()
    }
  }
}

object LoginTransitions : DestinationStyle.Animated {
  override fun AnimatedContentTransitionScope<NavBackStackEntry>.enterTransition(): EnterTransition {
    return when (initialState.appDestination()) {
      HomeDestination ->
        slideIntoContainer(
          towards = Start,
          animationSpec = tween(slideAnimationTween)
        ) + fadeIn()
      OauthDestination -> scaleIn(tween(500), initialScale = 0.5f) + fadeIn()
      else -> fadeIn()
    }
  }
  override fun AnimatedContentTransitionScope<NavBackStackEntry>.exitTransition(): ExitTransition {
    return when (targetState.appDestination()) {
      HomeDestination ->
        slideOutOfContainer(
          towards = End,
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

object StatusDetailTransitions : DestinationStyle.Animated {
  override fun AnimatedContentTransitionScope<NavBackStackEntry>.enterTransition(): EnterTransition {
    return when (initialState.appDestination()) {
      HomeDestination, StatusDetailDestination ->
        slideIntoContainer(
          towards = Start,
          animationSpec = tween(slideAnimationTween)
        )
      else -> fadeIn()
    }
  }
  override fun AnimatedContentTransitionScope<NavBackStackEntry>.exitTransition(): ExitTransition {
    return when (targetState.appDestination()) {
      HomeDestination ->
        slideOutOfContainer(
          towards = End,
          animationSpec = tween(slideAnimationTween)
        )
      StatusDetailDestination, ProfileDestination ->
        slideOutOfContainer(
          towards = Start,
          animationSpec = tween(slideAnimationTween)
        )
      else -> fadeOut()
    }
  }
  override fun AnimatedContentTransitionScope<NavBackStackEntry>.popEnterTransition(): EnterTransition {
    return when {
      initialState.appDestination() == StatusMediaScreenDestination -> fadeIn()
      else -> slideIntoContainer(
        towards = End,
        animationSpec = tween(slideAnimationTween)
      )
    }
  }
  override fun AnimatedContentTransitionScope<NavBackStackEntry>.popExitTransition(): ExitTransition {
    return when (targetState.appDestination()) {
      HomeDestination, StatusDetailDestination ->
        slideOutOfContainer(
          towards = End,
          animationSpec = tween(slideAnimationTween)
        )
      else -> fadeOut()
    }
  }
}

object StatusMediaTransitions : DestinationStyle.Animated {
  override fun AnimatedContentTransitionScope<NavBackStackEntry>.enterTransition(): EnterTransition {
    return fadeIn(tween(slideAnimationTween, easing = LinearEasing))
  }

  override fun AnimatedContentTransitionScope<NavBackStackEntry>.exitTransition(): ExitTransition {
    return fadeOut()
  }
}

object ProfileTransitions : DestinationStyle.Animated {
  override fun AnimatedContentTransitionScope<NavBackStackEntry>.enterTransition(): EnterTransition {
    return slideIntoContainer(
      towards = Start,
      animationSpec = tween(slideAnimationTween)
    ) + fadeIn()
  }
  override fun AnimatedContentTransitionScope<NavBackStackEntry>.exitTransition(): ExitTransition {
    return slideOutOfContainer(
      towards = End,
      animationSpec = tween(slideAnimationTween)
    ) + fadeOut()
  }
}

object PostTransitions : DestinationStyle.Animated {
  override fun AnimatedContentTransitionScope<NavBackStackEntry>.enterTransition(): EnterTransition {
    return slideIntoContainer(
      towards = Start,
      animationSpec = tween(slideAnimationTween)
    ) + fadeIn()
  }
  override fun AnimatedContentTransitionScope<NavBackStackEntry>.exitTransition(): ExitTransition {
    return slideOutOfContainer(
      towards = End,
      animationSpec = tween(slideAnimationTween)
    ) + fadeOut()
  }
}

const val slideAnimationTween = 300
