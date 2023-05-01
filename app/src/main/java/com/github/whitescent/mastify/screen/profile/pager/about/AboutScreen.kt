package com.github.whitescent.mastify.screen.profile.pager.about

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.whitescent.mastify.AppTheme
import com.github.whitescent.mastify.network.model.response.account.Profile

@Composable
fun AboutScreen(
  aboutModel: Profile?
) {
  Crossfade(targetState = aboutModel) {
    when (it) {
      null -> {
        CircularProgressIndicator()
      }
      else -> {
        Column(
          modifier = Modifier.fillMaxSize().padding(24.dp)
        ) {
          Text(
            text = "基本信息",
            style = AppTheme.typography.headlineSmall,
            color = AppTheme.colorScheme.onBackground.copy(alpha = 0.6f)
          )
        }
      }
    }
  }
}
