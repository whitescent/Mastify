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
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.whitescent.mastify.paging.factory.UnreadEvent
import com.github.whitescent.mastify.screen.NavGraphs
import com.github.whitescent.mastify.screen.appCurrentDestinationAsState
import com.github.whitescent.mastify.screen.destinations.Destination
import com.github.whitescent.mastify.screen.destinations.ExploreDestination
import com.github.whitescent.mastify.screen.destinations.LoginDestination
import com.github.whitescent.mastify.screen.destinations.NotificationDestination
import com.github.whitescent.mastify.screen.destinations.ProfileDestination
import com.github.whitescent.mastify.screen.destinations.SettingsDestination
import com.github.whitescent.mastify.screen.explore.Explore
import com.github.whitescent.mastify.screen.notification.Notification
import com.github.whitescent.mastify.screen.startAppDestination
import com.github.whitescent.mastify.ui.transitions.defaultSlideIntoContainer
import com.github.whitescent.mastify.ui.transitions.defaultSlideOutContainer
import com.github.whitescent.mastify.utils.isBottomBarScreen
import com.github.whitescent.mastify.utils.rememberAppState
import com.github.whitescent.mastify.utils.shouldShowScaffoldElements
import com.github.whitescent.mastify.viewModel.AppViewModel
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.defaults.RootNavGraphDefaultAnimations
import com.ramcosta.composedestinations.animations.rememberAnimatedNavHostEngine
import com.ramcosta.composedestinations.manualcomposablecalls.composable
import com.ramcosta.composedestinations.navigation.dependency
import com.ramcosta.composedestinations.navigation.navigate
import com.ramcosta.composedestinations.navigation.popUpTo
import com.ramcosta.composedestinations.scope.resultRecipient
import com.ramcosta.composedestinations.spec.Route
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterialNavigationApi::class, ExperimentalAnimationApi::class)
@Composable
fun AppScaffold(
  startRoute: Route,
  viewModel: AppViewModel
) {
  val accounts by viewModel.accountList.collectAsStateWithLifecycle()
  val activeAccount by viewModel.activeAccount.collectAsStateWithLifecycle()

  val engine = rememberAnimatedNavHostEngine(
    rootDefaultAnimations = RootNavGraphDefaultAnimations(
      enterTransition = {
        defaultSlideIntoContainer()
      },
      exitTransition = {
        defaultSlideOutContainer()
      },
      popEnterTransition = {
        defaultSlideIntoContainer(forward = false)
      },
      popExitTransition = {
        defaultSlideOutContainer(forward = false)
      }
    )
  )
  val navController = engine.rememberNavController()
  val scope = rememberCoroutineScope()
  val drawerState = rememberDrawerState(DrawerValue.Closed)
  val appState = rememberAppState()

  val destination: Destination = navController.appCurrentDestinationAsState().value
    ?: startRoute.startAppDestination

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
          },
          navigateToSettings = {
            navController.navigate(SettingsDestination) {
              scope.launch {
                drawerState.close()
              }
            }
          }
        )
      }
    },
    gesturesEnabled = destination.shouldShowScaffoldElements(),
    modifier = Modifier.fillMaxSize()
  ) {
    Scaffold(
      bottomBar = {
        if (destination.shouldShowScaffoldElements() && !appState.hideBottomBar) {
          BottomBar(
            appState = appState,
            navController = navController,
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
        navGraph = NavGraphs.root,
        startRoute = startRoute,
        dependenciesContainerBuilder = {
          dependency(NavGraphs.app) { drawerState }
          dependency(NavGraphs.app) { appState }
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
