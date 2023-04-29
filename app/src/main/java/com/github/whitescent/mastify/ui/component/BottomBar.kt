package com.github.whitescent.mastify.ui.component

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import com.github.whitescent.mastify.utils.BottomBarItem

@Composable
fun BottomBar(
  navController: NavController
) {
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentDestination = navBackStackEntry?.destination
  NavigationBar {
    BottomBarItem.values().forEachIndexed { _, screen ->
      NavigationBarItem(
        selected = currentDestination?.route == screen.route,
        onClick = {
          navController.navigate(screen.route) {
            popUpTo(currentDestination!!.route!!) {
              saveState = true
              inclusive = true
            }
            restoreState = true
          }
        },
        icon = {
          Icon(
            painter = painterResource(
              id = if (currentDestination?.hierarchy?.any { it.route == screen.route } == true)
                screen.selectedIcon else screen.unselectedIcon
            ),
            contentDescription = null,
            modifier = Modifier.size(24.dp)
          )
        },
        alwaysShowLabel = false
      )
    }
  }
}
