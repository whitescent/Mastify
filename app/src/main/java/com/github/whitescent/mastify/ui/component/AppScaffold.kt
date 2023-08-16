package com.github.whitescent.mastify.ui.component

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.whitescent.mastify.screen.NavGraphs
import com.github.whitescent.mastify.screen.appCurrentDestinationAsState
import com.github.whitescent.mastify.screen.destinations.Destination
import com.github.whitescent.mastify.screen.destinations.LoginDestination
import com.github.whitescent.mastify.screen.destinations.ProfileDestination
import com.github.whitescent.mastify.screen.startAppDestination
import com.github.whitescent.mastify.ui.theme.AppTheme
import com.github.whitescent.mastify.utils.rememberAppState
import com.github.whitescent.mastify.utils.shouldShowScaffoldElements
import com.github.whitescent.mastify.viewModel.AppViewModel
import com.google.accompanist.systemuicontroller.SystemUiController
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.navigation.dependency
import com.ramcosta.composedestinations.navigation.navigate
import com.ramcosta.composedestinations.navigation.popUpTo
import com.ramcosta.composedestinations.rememberNavHostEngine
import com.ramcosta.composedestinations.spec.Route
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(FlowPreview::class)
@Composable
fun AppScaffold(
  startRoute: Route,
  systemUiController: SystemUiController,
  viewModel: AppViewModel = hiltViewModel()
) {
  val engine = rememberNavHostEngine()
  val navController = engine.rememberNavController()
  val scope = rememberCoroutineScope()
  val drawerState = rememberDrawerState(DrawerValue.Closed)
  val lazyState = rememberLazyListState(
    initialFirstVisibleItemIndex = viewModel.timelineScrollPosition ?: 0,
    initialFirstVisibleItemScrollOffset = viewModel.timelineScrollPositionOffset ?: 0
  )

  val destination: Destination = navController.appCurrentDestinationAsState().value
    ?: startRoute.startAppDestination

  ModalNavigationDrawer(
    drawerState = drawerState,
    drawerContent = {
      if (destination.shouldShowScaffoldElements() && viewModel.activeAccount != null) {
        AppDrawer(
          isSystemBarVisible = systemUiController.isSystemBarsVisible,
          drawerState = drawerState,
          activeAccount = viewModel.activeAccount!!,
          accounts = viewModel.accounts.toImmutableList(),
          changeAccount = {
            viewModel.changeActiveAccount(it)
            navController.navigate(NavGraphs.app) {
              scope.launch {
                drawerState.close()
                lazyState.scrollToItem(0)
              }
              popUpTo(NavGraphs.root)
            }
          },
          navigateToLogin = {
            navController.navigate(LoginDestination) {
              scope.launch {
                drawerState.close()
              }
            }
          },
          navigateToProfile = {
            navController.navigate(ProfileDestination(it)) {
              scope.launch {
                drawerState.close()
              }
            }
          }
        )
      }
    },
    gesturesEnabled = destination.shouldShowScaffoldElements()
  ) {
    Scaffold(
      bottomBar = {
        if (destination.shouldShowScaffoldElements()) {
          BottomBar(
            navController = navController,
            destination = destination,
            scrollToTop = {
              scope.launch { lazyState.scrollToItem(0) }
            },
          )
        }
      },
      containerColor = AppTheme.colors.background
    ) {
      val appState = rememberAppState(it.calculateTopPadding(), it.calculateBottomPadding())
      DestinationsNavHost(
        engine = engine,
        navController = navController,
        navGraph = NavGraphs.root,
        startRoute = startRoute,
        dependenciesContainerBuilder = {
          dependency(NavGraphs.app) { lazyState }
          dependency(NavGraphs.app) { drawerState }
          dependency(NavGraphs.app) { appState }
        }
      )
      LaunchedEffect(it) {
        appState.setPaddingValues(it)
      }
    }
  }

  LaunchedEffect(Unit) {
    snapshotFlow { lazyState.firstVisibleItemIndex }
      .debounce(500L)
      .collectLatest {
        viewModel.saveTimelineScrollPosition(it, lazyState.firstVisibleItemScrollOffset)
      }
  }

  BackHandler(drawerState.isOpen) {
    scope.launch {
      drawerState.close()
    }
  }
}
