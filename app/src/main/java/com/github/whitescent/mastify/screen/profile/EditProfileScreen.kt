package com.github.whitescent.mastify.screen.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.github.whitescent.mastify.NonBottomBarNavGraph
import com.ramcosta.composedestinations.annotation.Destination

@NonBottomBarNavGraph(start = true)
@Destination
@Composable
fun EditProfileScreen() {
  Box(
    modifier = Modifier.fillMaxSize().background(Color.Blue)
  ) {

  }
}
