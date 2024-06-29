package com.github.whitescent.mastify.core.navigation

import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import com.github.whitescent.mastify.core.navigation.Route.Foundation.Discover
import com.github.whitescent.mastify.core.navigation.Route.Foundation.Home
import com.github.whitescent.mastify.core.navigation.Route.Foundation.Notifications
import kotlinx.serialization.Serializable

sealed interface Route {

  @Serializable
  data object Foundation : Route {
    @Serializable
    data object Home : Route

    @Serializable
    data object Discover : Route

    @Serializable
    data object Notifications : Route

    val entries = mapOf(
      R.drawable.home to Home,
      R.drawable.discover to Discover,
      R.drawable.notification to Notifications
    )
  }

  @Serializable
  data object Login : Route
}

fun NavController.currentRoute(): Route {
  val destination = requireNotNull(currentBackStackEntry?.destination) {
    "NavController has no current destination"
  }
  return when {
    destination.hasRoute<Home>() -> Home
    destination.hasRoute<Discover>() -> Discover
    destination.hasRoute<Notifications>() -> Notifications
    destination.hasRoute<Route.Login>() -> Route.Login
    else -> error("Unknown route: ${destination.route}")
  }
}
