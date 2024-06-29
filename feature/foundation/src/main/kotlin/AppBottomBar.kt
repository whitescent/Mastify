package com.github.whitescent.mastify.feature.foundation

import android.view.animation.OvershootInterpolator
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import com.github.whitescent.mastify.core.common.compose.clickableWithoutRipple
import com.github.whitescent.mastify.core.common.compose.thenIf
import com.github.whitescent.mastify.core.navigation.Route
import com.github.whitescent.mastify.core.ui.AppTheme
import com.github.whitescent.mastify.core.ui.component.CenterRow

@Composable
fun AppBottomBar(
  navController: NavController,
  scrollToTop: () -> Unit,
  modifier: Modifier = Modifier,
) = Surface(
  modifier = modifier
    .fillMaxWidth()
    .shadow(24.dp, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
  shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
  color = AppTheme.colors.bottomBarBackground,
) {
  val navBackStackEntry = navController.currentBackStackEntryAsState().value
  val currentRoute = navBackStackEntry?.destination
  CenterRow(Modifier.navigationBarsPadding()) {
    Route.Foundation.entries.forEach { screen ->
      val selected = currentRoute?.hierarchy?.any { it.hasRoute(screen.value::class) }
      BottomBarIcon(
        icon = {
          AnimatedContent(
            targetState = selected,
            transitionSpec = {
              ContentTransform(
                targetContentEnter = scaleIn(
                  animationSpec = tween(
                    durationMillis = 340,
                    easing = overshootEasing()
                  ),
                ),
                initialContentExit = fadeOut(tween(277)),
              ).using(SizeTransform(clip = false))
            },
          ) { isSelected ->
            Icon(
              painter = painterResource(screen.key),
              contentDescription = null,
              modifier = modifier
                .size(24.dp)
                .thenIf(isSelected == false) {
                  alpha(0.2f)
                },
              tint = AppTheme.colors.primaryContent
            )
          }
        },
        modifier = Modifier.clickableWithoutRipple {
          when (selected == true) {
            true -> scrollToTop()
            false -> {
              navController.navigate(screen.value) {
                popUpTo(currentRoute!!.route!!) {
                  saveState = true
                  inclusive = true
                }
                restoreState = true
              }
            }
          }
        }.padding(24.dp)
      )
    }
  }
}

@Composable
private fun RowScope.BottomBarIcon(
  icon: @Composable () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier.weight(1f),
    contentAlignment = Alignment.Center
  ) {
    icon()
  }
}

private fun overshootEasing(tension: Float = 1.9f) = Easing {
  OvershootInterpolator(tension).getInterpolation(it)
}
