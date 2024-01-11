/*
 * Copyright 2024 WhiteScent
 *
 * This file is a part of Mastify.
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * Mastify is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Mastify; if not,
 * see <http://www.gnu.org/licenses>.
 */

package com.github.whitescent.mastify.utils

import androidx.annotation.DrawableRes
import com.github.whitescent.R
import com.github.whitescent.mastify.screen.destinations.DirectMessageDestination
import com.github.whitescent.mastify.screen.destinations.ExploreDestination
import com.github.whitescent.mastify.screen.destinations.HomeDestination
import com.github.whitescent.mastify.screen.destinations.NotificationDestination
import com.ramcosta.composedestinations.spec.DirectionDestinationSpec

enum class BottomBarItem(
  @DrawableRes val icon: Int,
  val direction: DirectionDestinationSpec,
) {
  Home(R.drawable.home, HomeDestination),
  Explore(R.drawable.explore, ExploreDestination),
  Notification(R.drawable.notification, NotificationDestination),
  DirectMessage(R.drawable.directmessage, DirectMessageDestination)
}
