package com.github.whitescent.mastify.screen.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.github.whitescent.mastify.data.repository.ApiRepository
import com.github.whitescent.mastify.data.repository.PreferenceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
  private val preferenceRepository: PreferenceRepository
) : ViewModel() {

  private val token = preferenceRepository.accessToken.asStateFlow()

  private val _uiState = MutableStateFlow(HomeUiState())
  val uiState = _uiState.asStateFlow()

  init {
    token.value?.let {
      _uiState.value = _uiState.value.copy(isLoggedIn = true)
    }
  }

}

data class HomeUiState(
  val isLoggedIn: Boolean = false
)
