package com.github.whitescent.mastify.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.github.whitescent.mastify.NavGraphs
import com.github.whitescent.mastify.appCurrentDestinationAsState
import com.github.whitescent.mastify.destinations.Destination
import com.github.whitescent.mastify.startAppDestination
import com.github.whitescent.mastify.ui.theme.AppTheme
import com.github.whitescent.mastify.utils.BottomBarItem
import com.ramcosta.composedestinations.navigation.navigate

@Composable
fun BottomBar(
  navController: NavController,
  onClickHome: () -> Unit
) {

  val currentDestination: Destination = navController.appCurrentDestinationAsState().value
    ?: NavGraphs.bottomBar.startAppDestination
  
  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .heightIn(54.dp)
      .shadow(24.dp, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
    color = AppTheme.colors.bottomBarBackground,
  ) {
    CenterRow {
      BottomBarItem.values().forEachIndexed { _, screen ->
        Column(
          modifier = Modifier
            .weight(1f)
            .clickable(
              onClick = {
                if (currentDestination.route == screen.direction.route) {
                  onClickHome()
                }
                navController.navigate(screen.direction) {
                  popUpTo(currentDestination.route) {
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
            icon = screen.icon,
            selected = currentDestination == screen.direction,
            modifier = Modifier
          )
        }
      }
    }
  }
}

@Composable
private fun BottomBarIcon(
  @DrawableRes icon: Int,
  selected: Boolean,
  modifier: Modifier = Modifier
) {
  Icon(
    painter = painterResource(icon),
    contentDescription = null,
    modifier = modifier
      .size(24.dp)
      .let {
        if (!selected) it.alpha(0.2f) else it
      },
    tint = AppTheme.colors.primaryContent
  )
}
