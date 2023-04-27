package com.github.whitescent.mastify.ui.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.github.whitescent.mastify.screen.home.HomeScreen
import com.github.whitescent.mastify.screen.notification.NotificationScreen
import com.github.whitescent.mastify.utils.BottomBarItem

@OptIn( ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
  mainNavHostController: NavHostController
) {

  val navController = rememberNavController()

  Scaffold(
    bottomBar = {
      BottomBar(
        navController = navController
      )
    }
  ) {
    NavHost(navController, startDestination = "home", Modifier.padding(it)) {
      composable(BottomBarItem.Home.route) {
        HomeScreen(mainNavHostController)
      }
      composable(BottomBarItem.Notification.route) {
        NotificationScreen()
      }
    }
  }
}
