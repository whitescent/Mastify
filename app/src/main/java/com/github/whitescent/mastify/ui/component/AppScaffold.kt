package com.github.whitescent.mastify.ui.component

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
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
import com.github.whitescent.mastify.screen.home.HomeScreenModel
import com.github.whitescent.mastify.screen.notification.NotificationScreen
import com.github.whitescent.mastify.ui.theme.AppTheme
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.manualcomposablecalls.composable
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
@AppNavGraph(start = true)
@BottomBarNavGraph
@Destination
@Composable
fun AppScaffold(
  topNavController: NavController,
  homeViewModel: HomeScreenModel = hiltViewModel()
) {

  val navController = rememberNavController()
  val scope = rememberCoroutineScope()
  val lazyState = rememberLazyListState(
    initialFirstVisibleItemIndex = homeViewModel.timelineScrollPosition ?: 0
  )

  Scaffold(
    bottomBar = {
      BottomBar(
        navController = navController,
        scrollToTop = {
          scope.launch { lazyState.scrollToItem(0) }
        }
      )
    },
    containerColor = AppTheme.colors.background
  ) {
    DestinationsNavHost(
      navController = navController,
      modifier = Modifier.padding(bottom = it.calculateBottomPadding()),
      navGraph = NavGraphs.bottomBar
    ) {
      composable(HomeScreenDestination) {
        HomeScreen(
          viewModel = homeViewModel,
          lazyState = lazyState,
          topNavController = topNavController
        )
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

  LaunchedEffect(lazyState) {
    snapshotFlow { lazyState.firstVisibleItemIndex }
      .debounce(500L)
      .collectLatest {
        homeViewModel.saveTimelineScrollPosition(it)
      }
  }

}
