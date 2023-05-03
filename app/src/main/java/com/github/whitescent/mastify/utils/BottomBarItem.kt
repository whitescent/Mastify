package com.github.whitescent.mastify.utils

import androidx.annotation.DrawableRes
import com.github.whitescent.R

enum class BottomBarItem(
  @DrawableRes val unselectedIcon: Int,
  @DrawableRes val selectedIcon: Int,
  val route: String
) {
  Home(R.drawable.home_2_line, R.drawable.home_2_fill, "home"),
  Notification(R.drawable.notification_4_line, R.drawable.notification_4_fill, "notifications"),
  Profile(R.drawable.user_3_line, R.drawable.user_3_fill, "profile")
}
