/*
 * Copyright 2023 WhiteScent
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
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Companion.End
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection.Companion.Start
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.whitescent.mastify.screen.NavGraphs
import com.github.whitescent.mastify.screen.appCurrentDestinationAsState
import com.github.whitescent.mastify.screen.destinations.Destination
import com.github.whitescent.mastify.screen.destinations.LoginDestination
import com.github.whitescent.mastify.screen.destinations.ProfileDestination
import com.github.whitescent.mastify.screen.startAppDestination
import com.github.whitescent.mastify.ui.theme.AppTheme
import com.github.whitescent.mastify.ui.transitions.slideAnimationTween
import com.github.whitescent.mastify.utils.rememberAppState
import com.github.whitescent.mastify.utils.shouldShowScaffoldElements
import com.github.whitescent.mastify.viewModel.AppViewModel
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.defaults.RootNavGraphDefaultAnimations
import com.ramcosta.composedestinations.animations.rememberAnimatedNavHostEngine
import com.ramcosta.composedestinations.navigation.dependency
import com.ramcosta.composedestinations.navigation.navigate
import com.ramcosta.composedestinations.navigation.popUpTo
import com.ramcosta.composedestinations.spec.Route
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialNavigationApi::class, ExperimentalAnimationApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AppScaffold(
  startRoute: Route,
  viewModel: AppViewModel = hiltViewModel()
) {
  val engine = rememberAnimatedNavHostEngine(
    rootDefaultAnimations = RootNavGraphDefaultAnimations(
      enterTransition = {
        slideIntoContainer(
          towards = Start,
          animationSpec = tween(slideAnimationTween)
        ) + fadeIn()
      },
      exitTransition = {
        slideOutOfContainer(
          towards = Start,
          animationSpec = tween(slideAnimationTween)
        ) + fadeOut()
      },
      popEnterTransition = {
        slideIntoContainer(
          towards = End,
          animationSpec = tween(slideAnimationTween)
        ) + fadeIn()
      },
      popExitTransition = {
        slideOutOfContainer(
          towards = End,
          animationSpec = tween(slideAnimationTween)
        ) + fadeOut()
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
      if (destination.shouldShowScaffoldElements() && viewModel.activeAccount != null) {
        AppDrawer(
          drawerState = drawerState,
          activeAccount = viewModel.activeAccount!!,
          accounts = viewModel.accounts.toImmutableList(),
          changeAccount = {
            scope.launch { drawerState.close() }
            viewModel.changeActiveAccount(it)
            navController.navigate(NavGraphs.app) {
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
              scope.launch { appState.scrollToTop() }
            },
          )
        }
      },
      containerColor = AppTheme.colors.background
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
      )
      LaunchedEffect(it) {
        appState.setPaddingValues(it)
      }
    }
  }

  BackHandler(drawerState.isOpen) {
    scope.launch {
      drawerState.close()
    }
  }
}
