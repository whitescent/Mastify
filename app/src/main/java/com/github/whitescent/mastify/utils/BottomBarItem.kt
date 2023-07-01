package com.github.whitescent.mastify.utils

import androidx.annotation.DrawableRes
import com.github.whitescent.R
import com.github.whitescent.mastify.screen.destinations.DirectMessageDestination
import com.github.whitescent.mastify.screen.destinations.ExplorerDestination
import com.github.whitescent.mastify.screen.destinations.HomeDestination
import com.github.whitescent.mastify.screen.destinations.NotificationDestination
import com.ramcosta.composedestinations.spec.DirectionDestinationSpec

enum class BottomBarItem(
  @DrawableRes val icon: Int,
  val direction: DirectionDestinationSpec,
) {
  Home(R.drawable.home, HomeDestination),
  Explorer(R.drawable.explore, ExplorerDestination),
  Notification(R.drawable.notification, NotificationDestination),
  DirectMessage(R.drawable.directmessage, DirectMessageDestination)
}
