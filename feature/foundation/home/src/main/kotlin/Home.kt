package com.github.whitescent.mastify.feature.foundation.hone

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.github.whitescent.mastify.core.ui.currentColorScheme

@Composable
fun Home() = Box(
  modifier = Modifier.fillMaxSize().background(currentColorScheme.background)
) {
  Text("Home")
}
