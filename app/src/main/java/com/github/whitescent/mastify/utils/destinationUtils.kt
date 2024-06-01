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

import com.github.whitescent.mastify.screen.destinations.Destination
import com.github.whitescent.mastify.screen.destinations.DirectMessageDestination
import com.github.whitescent.mastify.screen.destinations.ExploreDestination
import com.github.whitescent.mastify.screen.destinations.HomeDestination
import com.github.whitescent.mastify.screen.destinations.LoginDestination
import com.github.whitescent.mastify.screen.destinations.NotificationDestination
import com.github.whitescent.mastify.screen.destinations.OauthDestination
import com.github.whitescent.mastify.screen.destinations.StatusMediaScreenDestination

val Destination.isBottomBarScreen: Boolean
  get() = this == HomeDestination || this == ExploreDestination || this == NotificationDestination ||
    this == DirectMessageDestination

val Destination.isSharedElementTransition: Boolean
  get() = this == StatusMediaScreenDestination

fun Destination.shouldShowScaffoldElements(): Boolean {
  if (this is LoginDestination || this is OauthDestination) return false
  else {
    BottomBarItem.entries.forEach {
      if (this == it.direction) return true
    }
  }
  return false
}
