package com.github.whitescent.mastify.utils

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.github.whitescent.R

enum class BottomBarItem(
  @DrawableRes val unselectedIcon: Int,
  @DrawableRes val selectedIcon: Int,
  val route: String
) {
  Home(R.drawable.home, R.drawable.home_fill, "home"),
  Notification(R.drawable.notification, R.drawable.notification_fill, "notifications"),
}
