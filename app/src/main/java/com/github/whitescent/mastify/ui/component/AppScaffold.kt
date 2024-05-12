/*
 * Copyright 2024 WhiteScent
 *
 * This file is a part of Mastify.
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * Mastify is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Mastify; if not,
 * see <http://www.gnu.org/licenses>.
 */

package com.github.whitescent.mastify.ui.component

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.whitescent.mastify.paging.factory.UnreadEvent
import com.github.whitescent.mastify.screen.explore.Explore
import com.github.whitescent.mastify.screen.notification.Notification
import com.github.whitescent.mastify.ui.transitions.DefaultAppTransitions
import com.github.whitescent.mastify.utils.isBottomBarScreen
import com.github.whitescent.mastify.utils.rememberAppState
import com.github.whitescent.mastify.utils.shouldShowScaffoldElements
import com.github.whitescent.mastify.viewModel.AppViewModel
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.generated.NavGraphs
import com.ramcosta.composedestinations.generated.destinations.ExploreDestination
import com.ramcosta.composedestinations.generated.destinations.LoginRouteDestination
import com.ramcosta.composedestinations.generated.destinations.NotificationDestination
import com.ramcosta.composedestinations.generated.destinations.ProfileDestination
import com.ramcosta.composedestinations.generated.destinations.SettingsDestination
import com.ramcosta.composedestinations.manualcomposablecalls.composable
import com.ramcosta.composedestinations.navigation.dependency
import com.ramcosta.composedestinations.navigation.navGraph
import com.ramcosta.composedestinations.rememberNavHostEngine
import com.ramcosta.composedestinations.scope.resultRecipient
import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.spec.Route
import com.ramcosta.composedestinations.utils.currentDestinationAsState
import com.ramcosta.composedestinations.utils.rememberDestinationsNavigator
import com.ramcosta.composedestinations.utils.startDestination
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AppScaffold(
  startRoute: Route,
  viewModel: AppViewModel
) {
  val accounts by viewModel.accountList.collectAsStateWithLifecycle()
  val activeAccount by viewModel.activeAccount.collectAsStateWithLifecycle()

  val engine = rememberNavHostEngine()
  val navController = engine.rememberNavController()
  val navigator = navController.rememberDestinationsNavigator()

  val destination: DestinationSpec = navController.currentDestinationAsState().value ?:
    startRoute.startDestination

  val scope = rememberCoroutineScope()
  val drawerState = rememberDrawerState(DrawerValue.Closed)
  val appState = rememberAppState()

  SharedTransitionLayout {
    ModalNavigationDrawer(
      drawerState = drawerState,
      drawerContent = {
        if (destination.shouldShowScaffoldElements() && activeAccount != null) {
          AppDrawer(
            drawerState = drawerState,
            activeAccount = activeAccount!!,
            accounts = accounts.toImmutableList(),
            changeAccount = {
              scope.launch { drawerState.close() }
              viewModel.changeActiveAccount(it)
            },
            navigateToLogin = {
              navigator.navigate(LoginRouteDestination) {
                scope.launch {
                  drawerState.close()
                }
              }
            },
            navigateToProfile = {
              navigator.navigate(ProfileDestination(it)) {
                scope.launch {
                  drawerState.close()
                }
              }
            },
            navigateToSettings = {
              navigator.navigate(SettingsDestination) {
                scope.launch {
                  drawerState.close()
                }
              }
            }
          )
        }
      },
      gesturesEnabled = destination.shouldShowScaffoldElements(),
      modifier = Modifier
        .fillMaxSize()
        .semantics {
          testTagsAsResourceId = true
        }
    ) {
      Scaffold(
        bottomBar = {
          AnimatedVisibility(
            visible = destination.shouldShowScaffoldElements() && !appState.hideBottomBar,
            enter = slideInVertically { it },
            exit = slideOutVertically { it }
          ) {
            BottomBar(
              appState = appState,
              navigator = navigator,
              destination = destination,
              scrollToTop = {
                scope.launch { appState.scrollToTop() }
              }
            )
          }
        },
        containerColor = Color.Black,
        modifier = Modifier.fillMaxSize()
      ) {
        DestinationsNavHost(
          engine = engine,
          navController = navController,
          defaultTransitions = DefaultAppTransitions,
          navGraph = NavGraphs.root,
          startRoute = startRoute,
          dependenciesContainerBuilder = {
            navGraph(NavGraphs.app) {
              dependency(drawerState)
              dependency(appState)
            }
            dependency(this@SharedTransitionLayout)
          }
        ) {
          composable(ExploreDestination) {
            Explore(
              appState = appState,
              activeAccount = activeAccount!!,
              drawerState = drawerState,
              navigator = destinationsNavigator,
              resultRecipient = resultRecipient()
            )
          }
          composable(NotificationDestination) {
            Notification(
              activeAccount = activeAccount!!,
              drawerState = drawerState,
              appState = appState,
              navigator = destinationsNavigator
            )
          }
        }
      }
    }
  }

  LaunchedEffect(Unit) {
    launch {
      viewModel.changeAccountFlow.collect {
        navController.navigate(navController.currentDestination!!.route!!) {
          popUpTo(NavGraphs.app) { inclusive = true }
          NavGraphs.app.destinations.forEach {
            if (it.isBottomBarScreen) navController.clearBackStack(it.route)
          }
        }
      }
    }
    launch {
      viewModel.unreadFlow.collect {
        when (it) {
          is UnreadEvent.DismissAll -> appState.unreadNotifications = 0
          else -> Unit
        }
      }
    }
  }

  BackHandler(drawerState.isOpen) {
    scope.launch {
      drawerState.close()
    }
  }
}

val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope> {
  error("No AnimatedVisibilityScope provided")
}

val LocalAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope> {
  error("No AnimatedVisibilityScope provided")
}
