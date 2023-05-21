package com.github.whitescent.mastify.ui.component

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.github.whitescent.mastify.AppNavGraph
import com.github.whitescent.mastify.BottomBarNavGraph
import com.github.whitescent.mastify.NavGraphs
import com.github.whitescent.mastify.destinations.DirectMessageScreenDestination
import com.github.whitescent.mastify.destinations.ExplorerScreenDestination
import com.github.whitescent.mastify.destinations.HomeScreenDestination
import com.github.whitescent.mastify.destinations.NotificationScreenDestination
import com.github.whitescent.mastify.screen.directMessage.DirectMessageScreen
import com.github.whitescent.mastify.screen.explorer.ExplorerScreen
import com.github.whitescent.mastify.screen.home.HomeScreen
import com.github.whitescent.mastify.screen.notification.NotificationScreen
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.manualcomposablecalls.composable
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@AppNavGraph(start = true)
@BottomBarNavGraph
@Destination
@Composable
fun AppScaffold(
  primaryNavController: NavController,
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
    DestinationsNavHost(
      navController = navController,
      modifier = Modifier.padding(bottom = it.calculateBottomPadding()),
      navGraph = NavGraphs.bottomBar
    ) {
      composable(HomeScreenDestination) {
        HomeScreen(lazyState = lazyState, navController = primaryNavController)
      }
      composable(ExplorerScreenDestination) {
        ExplorerScreen()
      }
      composable(NotificationScreenDestination) {
        NotificationScreen()
      }
      composable(DirectMessageScreenDestination) {
        DirectMessageScreen()
      }
    }
  }
}
