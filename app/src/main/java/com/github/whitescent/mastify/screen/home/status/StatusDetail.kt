package com.github.whitescent.mastify.screen.home.status

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.github.whitescent.mastify.AppNavGraph
import com.github.whitescent.mastify.ui.theme.AppTheme
import com.github.whitescent.mastify.ui.transitions.StatusTransitions
import com.ramcosta.composedestinations.annotation.Destination

@AppNavGraph
@Destination(style = StatusTransitions::class)
@Composable
fun StatusDetail() {
  Box(Modifier.fillMaxSize().background(AppTheme.colors.cardBackground))
}
