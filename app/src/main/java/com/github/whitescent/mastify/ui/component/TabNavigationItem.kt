package com.github.whitescent.mastify.ui.component

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab

@Composable
fun RowScope.TabNavigationItem(tab: Tab) {
  val tabNavigator = LocalTabNavigator.current
  NavigationBarItem(
    selected = tabNavigator.current == tab,
    onClick = { tabNavigator.current = tab },
    icon = {
      Icon(
        painter = tab.options.icon!!,
        contentDescription = null,
        modifier = Modifier.size(24.dp)
      )
    },
    label = { Text(tab.options.title) },
    alwaysShowLabel = false
  )
}