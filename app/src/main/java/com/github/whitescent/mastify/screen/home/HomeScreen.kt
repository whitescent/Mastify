package com.github.whitescent.mastify.screen.home


import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController

@Composable
fun HomeScreen(
  mainNavController: NavController,
  viewModel: HomeViewModel = hiltViewModel()
) {
  val state by viewModel.uiState.collectAsStateWithLifecycle()
  LaunchedEffect(state.isLoggedIn) {
    if (!state.isLoggedIn) {
      mainNavController.navigate("login")
    }
  }
}
