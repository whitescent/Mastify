package com.github.whitescent.mastify

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import com.github.whitescent.mastify.screen.login.LoginScreen
import com.github.whitescent.mastify.screen.login.OauthScreen
import com.github.whitescent.mastify.ui.component.AppScaffold

fun NavGraphBuilder.loginGraph(mainNavController: NavHostController) {
  composable(route = "login") { LoginScreen() }
  composable(
    route = "oauth",
    deepLinks = listOf(navDeepLink { uriPattern = "mastify://oauth?code={code}" })
  ) { backStackEntry ->
    OauthScreen(
      navController = mainNavController
    )
  }
}

fun NavGraphBuilder.appGraph(mainNavController: NavHostController) {
  composable("app") { AppScaffold(mainNavController) }
}
