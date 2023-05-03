package com.github.whitescent.mastify.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.github.whitescent.mastify.utils.BottomBarItem

@Composable
fun BottomBar(
  navController: NavController,
  onClickHome: () -> Unit
) {
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentDestination = navBackStackEntry?.destination

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .heightIn(48.dp)
      .background(Color.White)
  ) {
    Column {
      Divider(thickness = 0.6.dp)
      CenterRow {
        BottomBarItem.values().forEachIndexed { _, screen ->
          Column(
            modifier = Modifier
              .weight(1f)
              .clickable(
                onClick = {
                  if (screen.route == BottomBarItem.Home.route &&
                    currentDestination!!.route == BottomBarItem.Home.route
                  ) {
                    onClickHome()
                  }
                  navController.navigate(screen.route) {
                    popUpTo(currentDestination!!.route!!) {
                      saveState = true
                      inclusive = true
                    }
                    restoreState = true
                  }
                },
                indication = null,
                interactionSource = MutableInteractionSource()
              )
              .padding(bottom = 35.dp, top = 20.dp, start = 24.dp, end = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            BottomBarIcon(
              unselectedIcon = screen.unselectedIcon,
              selectedIcon = screen.selectedIcon,
              selected = currentDestination?.route == screen.route,
              modifier = Modifier
            )
          }
        }
      }
    }
  }
}

@Composable
private fun BottomBarIcon(
  unselectedIcon: Int,
  selectedIcon: Int,
  selected: Boolean,
  modifier: Modifier = Modifier
) {
  Icon(
    painter = painterResource(id = if (selected) selectedIcon else unselectedIcon),
    contentDescription = null,
    modifier = modifier
      .size(24.dp)
  )
}
