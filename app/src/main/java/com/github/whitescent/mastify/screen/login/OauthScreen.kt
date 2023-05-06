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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.whitescent.mastify.LoginNavGraph
import com.github.whitescent.mastify.destinations.AppScaffoldDestination
import com.github.whitescent.mastify.destinations.LoginScreenDestination
import com.github.whitescent.mastify.destinations.OauthScreenDestination
import com.github.whitescent.mastify.ui.component.CenterRow
import com.github.whitescent.mastify.ui.component.WidthSpacer
import com.ramcosta.composedestinations.annotation.DeepLink
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.popUpTo

@Composable
@LoginNavGraph
@Destination(
  deepLinks = [
    DeepLink(uriPattern = "mastify://oauth?code={code}")
  ]
)
fun OauthScreen(
  navigator: DestinationsNavigator,
  viewModel: OauthScreenModel = hiltViewModel()
) {
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
  DisposableEffect(Unit) {
    viewModel.code?.let {
      viewModel.getAccessToken {
        navigator.navigate(AppScaffoldDestination) {
          popUpTo(OauthScreenDestination) {
            inclusive = true
          }
          popUpTo(LoginScreenDestination) {
            inclusive = true
          }
        }
      }
    } ?: run {
      // If the user refuses OAuth, we need to navigate to the login screen
      navigator.navigate(LoginScreenDestination) {
        popUpTo(OauthScreenDestination) {
          inclusive = true
        }
      }
    }
    onDispose {  }
  }

  BackHandler(true) {
    activity?.finish()
  }
}
