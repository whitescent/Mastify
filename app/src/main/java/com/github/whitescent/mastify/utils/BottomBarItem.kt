package com.github.whitescent.mastify.utils

import androidx.annotation.DrawableRes
import com.github.whitescent.R
import com.github.whitescent.mastify.destinations.DirectMessageScreenDestination
import com.github.whitescent.mastify.destinations.ExplorerScreenDestination
import com.github.whitescent.mastify.destinations.HomeScreenDestination
import com.github.whitescent.mastify.destinations.NotificationScreenDestination
import com.ramcosta.composedestinations.spec.DirectionDestinationSpec

enum class BottomBarItem(
  @DrawableRes val icon: Int,
  val direction: DirectionDestinationSpec,
) {
  Home(R.drawable.home, HomeScreenDestination),
  Explorer(R.drawable.explore, ExplorerScreenDestination),
  Notification(R.drawable.notification, NotificationScreenDestination),
  DirectMessage(R.drawable.directmessage, DirectMessageScreenDestination)
}
