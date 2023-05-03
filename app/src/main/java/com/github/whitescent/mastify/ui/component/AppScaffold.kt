package com.github.whitescent.mastify.ui.component

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.github.whitescent.mastify.screen.home.HomeScreen
import com.github.whitescent.mastify.screen.notification.NotificationScreen
import com.github.whitescent.mastify.screen.profile.ProfileScreen
import com.github.whitescent.mastify.utils.BottomBarItem
import kotlinx.coroutines.launch

@OptIn( ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
  mainNavHostController: NavHostController
) {

  val navController = rememberNavController()
  val lazyState = rememberLazyListState()
  val scope = rememberCoroutineScope()

  Scaffold(
    bottomBar = {
      BottomBar(
        navController = navController,
        onClickHome = {
          scope.launch { lazyState.scrollToItem(0) }
        }
      )
    }
  ) {
    NavHost(navController, startDestination = "home", Modifier.padding(bottom = it.calculateBottomPadding())) {
      composable(BottomBarItem.Home.route) {
        HomeScreen(mainNavHostController, lazyState)
      }
      composable(BottomBarItem.Notification.route) {
        NotificationScreen()
      }
      composable(BottomBarItem.Profile.route) {
        ProfileScreen()
      }
    }
  }
}
