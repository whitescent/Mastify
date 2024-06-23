package com.github.whitescent.mastify.feature.foundation.common

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.github.whitescent.mastify.core.navigation.Route

fun NavGraphBuilder.foundationNavGraph(
  navController: NavController,
) = composable<Route.Foundation>(
  enterTransition = {
    fadeIn(animationSpec = tween(300))
  },
  exitTransition = {
    fadeOut()
  },

  popEnterTransition = {
    fadeIn(animationSpec = tween(300))
  },
  popExitTransition = {
    fadeOut()
  },
) {
  Foundation(navController)
}
