package com.github.whitescent.mastify.screen.login

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.androidx.AndroidScreen
import cafe.adriel.voyager.hilt.getViewModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.*
import com.github.whitescent.R
import com.github.whitescent.mastify.AppTheme
import com.github.whitescent.mastify.ui.component.CenterRow
import com.github.whitescent.mastify.ui.component.HeightSpacer
import com.github.whitescent.mastify.ui.component.WidthSpacer

class LoginScreen: AndroidScreen() {
  @OptIn(ExperimentalMaterial3Api::class)
  @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
  @Composable
  override fun Content() {
    val navigator = LocalNavigator.currentOrThrow
    val viewModel = getViewModel<LoginViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    Box(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .padding(30.dp)
        .imePadding(),
    ) {
      Column(
        modifier = Modifier.fillMaxWidth()
      ) {
        CenterRow {
          Surface(
            shape = CircleShape,
            modifier = Modifier.size(48.dp)
          ) {
            Image(
              painter = painterResource(id = R.drawable.logo),
              contentDescription = null
            )
          }
          WidthSpacer(value = 12.dp)
          Text(
            text = stringResource(id = R.string.app_name),
            style = AppTheme.typography.headlineLarge
          )
        }
        HeightSpacer(value = 12.dp)
        Text(
          text = "登录",
          style = AppTheme.typography.titleLarge
        )
        HeightSpacer(value = 6.dp)
        Text(
          text = "请先输入您的实例服务器",
          style = AppTheme.typography.titleMedium
        )
        HeightSpacer(value = 16.dp)
        TextField(
          value = state.inputText,
          onValueChange = viewModel::onValueChange,
          leadingIcon = {
            Text(text = "https://")
          },
          trailingIcon = {
            AnimatedVisibility (state.inputText.isNotEmpty()) {
              IconButton(
                onClick = viewModel::clearInputText
              ) {
                Icon(
                  imageVector = Icons.Rounded.Close,
                  contentDescription = null,
                  tint = AppTheme.colorScheme.primary
                )
              }
            }
          },
          modifier = Modifier.fillMaxWidth(),
          colors = TextFieldDefaults.textFieldColors(
            containerColor = Color.Transparent
          )
        )
        HeightSpacer(value = 16.dp)
        Button(
          onClick = { /*TODO*/ },
          modifier = Modifier.fillMaxWidth()
        ) {

        }
      }
    }
  }
}
