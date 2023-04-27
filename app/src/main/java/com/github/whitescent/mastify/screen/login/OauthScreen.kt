package com.github.whitescent.mastify.screen.login

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.github.whitescent.mastify.ui.component.CenterRow
import com.github.whitescent.mastify.ui.component.WidthSpacer

@Composable
fun OauthScreen(
  navController: NavController,
  viewModel: OauthViewModel = hiltViewModel()
) {
  val token by viewModel.token.collectAsStateWithLifecycle()
  val activity = (LocalContext.current as? Activity)
  Dialog(
    onDismissRequest = { },
    properties = DialogProperties(
      usePlatformDefaultWidth = false
    )
  ) {
    Surface(
      shape = RoundedCornerShape(12.dp),
      color = Color.White,
      shadowElevation = 6.dp
    ) {
      CenterRow(
        modifier = Modifier.padding(24.dp)
      ) {
        Text(text = "正在完成身份认证...")
        WidthSpacer(value = 10.dp)
        CircularProgressIndicator(modifier = Modifier.size(24.dp))
      }
    }
  }
  LaunchedEffect(token) {
    token?.let {
      navController.popBackStack()
    }
  }
  BackHandler(true) {
    activity?.finish()
  }
}
