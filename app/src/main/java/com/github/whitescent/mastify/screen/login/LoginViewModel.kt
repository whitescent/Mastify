package com.github.whitescent.mastify.screen.login

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor() : ViewModel() {

  private val inputText = MutableStateFlow("")
  private val inputTextManager = MutableStateFlow(InputTextManager())

  private var _uiState = MutableStateFlow(LoginUiState())
  val uiState = _uiState.asStateFlow()

  fun onValueChange(text: String) {
    inputText.update { text }
    inputTextManager.value = inputTextManager.value.copy(text = text, isTyping = true)
    _uiState.value = _uiState.value.copy(
      inputText = inputTextManager.value.text,
      isInstanceError = inputTextManager.value.isInstanceError
    )
  }

  fun clearInputText() {
    inputText.value = ""
    inputTextManager.value = inputTextManager.value.copy(text = "", isTyping = false)
    _uiState.value = LoginUiState(
      inputText = inputTextManager.value.text,
      isInstanceError = false
    )
  }

}

data class InputTextManager(
  val text: String = "",
  val isTyping: Boolean = false,
  val isInstanceError: Boolean = false,
)

data class LoginUiState(
  val inputText: String = "",
  val isInstanceError: Boolean = false
)
