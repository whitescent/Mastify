package com.github.whitescent.mastify.ui.transitions

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.navigation.NavBackStackEntry
import com.github.whitescent.mastify.appDestination
import com.github.whitescent.mastify.destinations.AppScaffoldDestination
import com.ramcosta.composedestinations.spec.DestinationStyleAnimated


@OptIn(ExperimentalAnimationApi::class)
object AppTransitions : DestinationStyleAnimated {

  override fun AnimatedContentTransitionScope<NavBackStackEntry>.enterTransition(): EnterTransition {
    return when (initialState.appDestination()) {
      AppScaffoldDestination -> scaleIn(tween(500), initialScale = 0.5f) + fadeIn()
      else -> fadeIn()
    }
  }
  override fun AnimatedContentTransitionScope<NavBackStackEntry>.exitTransition(): ExitTransition {
    return when (targetState.appDestination()) {
      AppScaffoldDestination -> scaleOut(tween(500), targetScale = 0.5f) + fadeOut()
      else -> fadeOut()
    }
  }

}

@OptIn(ExperimentalAnimationApi::class)
object LoginTransitions : DestinationStyleAnimated {

  override fun AnimatedContentTransitionScope<NavBackStackEntry>.enterTransition(): EnterTransition {
    return fadeIn()
  }
  override fun AnimatedContentTransitionScope<NavBackStackEntry>.exitTransition(): ExitTransition {
    return fadeOut()
  }

}
