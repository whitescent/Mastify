package com.github.whitescent.mastify.utils

import androidx.annotation.DrawableRes
import com.github.whitescent.R
import com.github.whitescent.mastify.destinations.HomeScreenDestination
import com.github.whitescent.mastify.destinations.NotificationScreenDestination
import com.github.whitescent.mastify.destinations.ProfileScreenDestination
import com.ramcosta.composedestinations.spec.DirectionDestinationSpec

enum class BottomBarItem(
  @DrawableRes val unselectedIcon: Int,
  @DrawableRes val selectedIcon: Int,
  val direction: DirectionDestinationSpec,
) {
  Home(R.drawable.home, R.drawable.home_fill, HomeScreenDestination),
  Notification(R.drawable.notification, R.drawable.notification_fill, NotificationScreenDestination),
  Profile(R.drawable.user, R.drawable.user_fill, ProfileScreenDestination)
}
