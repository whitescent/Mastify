package com.github.whitescent.mastify.ui.component

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsIgnoringVisibility
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
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
import com.github.whitescent.mastify.destinations.*
import com.github.whitescent.mastify.screen.directMessage.DirectMessageScreen
import com.github.whitescent.mastify.screen.explorer.ExplorerScreen
import com.github.whitescent.mastify.screen.home.HomeScreen
import com.github.whitescent.mastify.screen.home.HomeScreenModel
import com.github.whitescent.mastify.screen.notification.NotificationScreen
import com.github.whitescent.mastify.ui.theme.AppTheme
import com.github.whitescent.mastify.ui.transitions.AppTransitions
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.manualcomposablecalls.composable
import com.ramcosta.composedestinations.navigation.navigate
import com.ramcosta.composedestinations.navigation.popUpTo
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class, ExperimentalLayoutApi::class)
@AppNavGraph(start = true)
@BottomBarNavGraph
@Destination(style = AppTransitions::class)
@Composable
fun AppScaffold(
  topNavController: NavController,
  homeViewModel: HomeScreenModel = hiltViewModel()
) {

  val navController = rememberNavController()
  val scope = rememberCoroutineScope()
  val drawerState = rememberDrawerState(DrawerValue.Closed)

  // issue: In many cases, initializing the first visible item index
  // still cannot accurately locate the saved position
  val lazyState = rememberLazyListState(
    initialFirstVisibleItemIndex = homeViewModel.timelineScrollPosition ?: 0,
    initialFirstVisibleItemScrollOffset = homeViewModel.timelineScrollPositionOffset ?: 0
  )

  ModalNavigationDrawer(
    drawerState = drawerState,
    drawerContent = {
      AppDrawer(
        drawerState = drawerState,
        activeAccount = homeViewModel.activeAccount,
        accounts = homeViewModel.accounts,
        changeAccount = {
          homeViewModel.changeActiveAccount(it)
          topNavController.navigate(AppScaffoldDestination) {
            popUpTo(NavGraphs.app)
          }
        },
        navigateToLogin = {
          topNavController.navigate(NavGraphs.login)
        }
      )
    }
  ) {
    Scaffold(
      bottomBar = {
        BottomBar(
          navController = navController,
          scrollToTop = {
            scope.launch { lazyState.scrollToItem(0) }
          }
        )
      },
      containerColor = AppTheme.colors.background,
      contentWindowInsets = WindowInsets.systemBarsIgnoringVisibility
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
            topNavController = topNavController,
            openDrawer = {
              scope.launch {
                drawerState.open()
              }
            }
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
  }

  LaunchedEffect(lazyState) {
    snapshotFlow { lazyState.firstVisibleItemIndex }
      .debounce(500L)
      .collectLatest {
        homeViewModel.saveTimelineScrollPosition(it, lazyState.firstVisibleItemScrollOffset)
      }
  }

  BackHandler(drawerState.isOpen) {
    scope.launch {
      drawerState.close()
    }
  }

}
