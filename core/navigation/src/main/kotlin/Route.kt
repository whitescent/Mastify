package com.github.whitescent.mastify.core.navigation

import kotlinx.serialization.Serializable

sealed interface Route {
  @Serializable
  data object App : Route
  @Serializable
  data object Login : Route {
    @Serializable
    data object LoginEnter : Route
    @Serializable
    data class Oauth(val code: String) : Route
  }
}

sealed interface AppRoute {
  @Serializable
  data object Home : AppRoute
}
