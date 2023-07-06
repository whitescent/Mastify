package com.github.whitescent.mastify.ui.component

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import com.github.whitescent.mastify.screen.NavGraphs
import com.github.whitescent.mastify.screen.appCurrentDestinationAsState
import com.github.whitescent.mastify.screen.destinations.Destination
import com.github.whitescent.mastify.screen.destinations.LoginDestination
import com.github.whitescent.mastify.screen.startAppDestination
import com.github.whitescent.mastify.ui.theme.AppTheme
import com.github.whitescent.mastify.ui.transitions.slideAnimationOffset
import com.github.whitescent.mastify.utils.shouldShowScaffoldElements
import com.github.whitescent.mastify.viewModel.AppViewModel
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.systemuicontroller.SystemUiController
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.rememberAnimatedNavHostEngine
import com.ramcosta.composedestinations.navigation.dependency
import com.ramcosta.composedestinations.navigation.navigate
import com.ramcosta.composedestinations.navigation.popUpTo
import com.ramcosta.composedestinations.spec.Route
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

@OptIn(
  FlowPreview::class,
  ExperimentalLayoutApi::class,
  ExperimentalMaterialNavigationApi::class,
  ExperimentalAnimationApi::class
)
@Composable
fun AppScaffold(
  startRoute: Route,
  systemUiController: SystemUiController,
  viewModel: AppViewModel = hiltViewModel()
) {
  val engine = rememberAnimatedNavHostEngine()
  val navController = rememberAnimatedNavController()
  val scope = rememberCoroutineScope()
  val drawerState = rememberDrawerState(DrawerValue.Closed)

  // issue: In many cases, initializing the first visible item index
  // still cannot accurately locate the saved position
  val lazyState = rememberLazyListState(
    initialFirstVisibleItemIndex = viewModel.timelineScrollPosition ?: 0,
    initialFirstVisibleItemScrollOffset = viewModel.timelineScrollPositionOffset ?: 0
  )

  val destination: Destination = navController.appCurrentDestinationAsState().value
    ?: startRoute.startAppDestination

  ModalNavigationDrawer(
    drawerState = drawerState,
    drawerContent = {
      if (destination.shouldShowScaffoldElements()) {
        AppDrawer(
          isSystemBarVisible = systemUiController.isSystemBarsVisible,
          drawerState = drawerState,
          activeAccount = viewModel.activeAccount!!,
          accounts = viewModel.accounts,
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
          }
        )
      }
    },
    gesturesEnabled = destination.shouldShowScaffoldElements()
  ) {
    Scaffold(
      bottomBar = {
        AnimatedVisibility(
          visible = destination.shouldShowScaffoldElements(),
          enter = fadeIn(tween(slideAnimationOffset)),
          exit = fadeOut(tween(slideAnimationOffset))
        ) {
          if (destination.shouldShowScaffoldElements()) {
            BottomBar(
              navController = navController,
              destination = destination,
              scrollToTop = {
                scope.launch { lazyState.scrollToItem(0) }
              }
            )
          }
        }
      },
      containerColor = AppTheme.colors.background,
      contentWindowInsets = WindowInsets.systemBarsIgnoringVisibility,
    ) {
      DestinationsNavHost(
        engine = engine,
        navController = navController,
        modifier = Modifier.padding(bottom = it.calculateBottomPadding()),
        navGraph = NavGraphs.root,
        startRoute = startRoute,
        dependenciesContainerBuilder = {
          dependency(NavGraphs.app) { lazyState }
          dependency(NavGraphs.app) { drawerState }
        }
      )
    }
  }

  LaunchedEffect(lazyState) {
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
