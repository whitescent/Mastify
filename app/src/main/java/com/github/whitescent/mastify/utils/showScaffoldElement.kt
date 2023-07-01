package com.github.whitescent.mastify.utils

import com.github.whitescent.mastify.screen.destinations.Destination
import com.github.whitescent.mastify.screen.destinations.LoginDestination
import com.github.whitescent.mastify.screen.destinations.OauthDestination

fun Destination.shouldShowScaffoldElements(): Boolean {
  if (this is LoginDestination || this is OauthDestination) return false
  else {
    BottomBarItem.values().forEach {
      if (this == it.direction) return true
    }
  }
  return false
}
