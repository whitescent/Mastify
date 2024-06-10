
package com.github.whitescent.mastify.feature.login

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.navigation
import com.github.whitescent.mastify.core.navigation.Route

fun NavGraphBuilder.loginNavGraph(
  navController: NavController
) {
  navigation<Route.Login>(
    startDestination = Route.Login.LoginEnter
  ) {
    composable<Route.Login.LoginEnter> {
      Login()
    }
    composable<Route.Login.Oauth>(
      deepLinks = listOf(
        navDeepLink {
          uriPattern = "mastify://oauth?code={code}"
        }
      )
    ) {
      Oauth(navController)
    }
  }
}
