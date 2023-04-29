package com.github.whitescent.mastify.utils

import androidx.annotation.DrawableRes
import com.github.whitescent.R

enum class BottomBarItem(
  @DrawableRes val unselectedIcon: Int,
  @DrawableRes val selectedIcon: Int,
  val route: String
) {
  Home(R.drawable.home, R.drawable.home_selected, "home"),
  Notification(R.drawable.notification, R.drawable.notification_selected, "notifications"),
  Profile(R.drawable.profile, R.drawable.profile_selected, "profile")
}
