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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.whitescent.R
import com.github.whitescent.mastify.LoginNavGraph
import com.github.whitescent.mastify.NavGraphs
import com.github.whitescent.mastify.destinations.AppScaffoldDestination
import com.github.whitescent.mastify.ui.component.CenterRow
import com.github.whitescent.mastify.ui.component.WidthSpacer
import com.github.whitescent.mastify.ui.theme.AppTheme
import com.ramcosta.composedestinations.annotation.DeepLink
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.popUpTo
import kotlinx.coroutines.delay

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
      color = AppTheme.colors.background,
      shadowElevation = 6.dp
    ) {
      CenterRow(
        modifier = Modifier.padding(24.dp)
      ) {
        Text(text = stringResource(id = R.string.Connecting), color = AppTheme.colors.primaryContent)
        WidthSpacer(value = 10.dp)
        CircularProgressIndicator(modifier = Modifier.size(24.dp))
      }
    }
  }
  LaunchedEffect(Unit) {
    delay(300)
    viewModel.code?.let {
      viewModel.fetchAccessToken(
        navigateToApp = {
          navigator.navigate(AppScaffoldDestination) {
            popUpTo(NavGraphs.root) {
              inclusive = true
            }
          }
        }
      )
    } ?: run {
      // If the user refuses OAuth, we need to navigate to the login screen
      navigator.popBackStack()
    }
  }
  BackHandler {
    activity?.finish()
  }
}
