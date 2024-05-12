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

import com.ramcosta.composedestinations.generated.NavGraphs
import com.ramcosta.composedestinations.generated.destinations.DirectMessageDestination
import com.ramcosta.composedestinations.generated.destinations.ExploreDestination
import com.ramcosta.composedestinations.generated.destinations.HomeDestination
import com.ramcosta.composedestinations.generated.destinations.NotificationDestination
import com.ramcosta.composedestinations.spec.DestinationSpec

val DestinationSpec.isBottomBarScreen: Boolean
  get() = this == HomeDestination || this == ExploreDestination || this == NotificationDestination ||
    this == DirectMessageDestination

fun DestinationSpec.shouldShowScaffoldElements(): Boolean {
  if (NavGraphs.login.destinations.contains(this)) return false
  else {
    BottomBarItem.entries.forEach {
      if (this == it.direction) return true
    }
  }
  return false
}
