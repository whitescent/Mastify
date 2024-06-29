package com.github.whitescent.mastify.feature.foundation

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.github.whitescent.mastify.core.navigation.Route
import com.github.whitescent.mastify.feature.foundation.hone.Home

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Foundation(
  navController: NavController
) {
  val scope = rememberCoroutineScope()
  val drawerState = rememberDrawerState(DrawerValue.Closed)
  val homeNavController = rememberNavController()
  ModalNavigationDrawer(
    drawerState = drawerState,
    drawerContent = {
    }
  ) {
    Scaffold(
      bottomBar = {
        AppBottomBar(
          navController = homeNavController,
          scrollToTop = {
          }
        )
      },
      containerColor = Color.Black,
      modifier = Modifier.fillMaxSize()
    ) {
      NavHost(
        navController = homeNavController,
        startDestination = Route.Foundation.Home
      ) {
        composable<Route.Foundation.Home> {
          Home()
        }
        composable<Route.Foundation.Discover> {
        }
        composable<Route.Foundation.Notifications> {
        }
      }
    }
  }
}
