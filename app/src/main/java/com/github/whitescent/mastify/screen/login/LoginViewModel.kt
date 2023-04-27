package com.github.whitescent.mastify.screen.login

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.whitescent.mastify.data.repository.ApiRepository
import com.github.whitescent.mastify.data.repository.PreferenceRepository
import com.github.whitescent.mastify.network.model.request.ClientInfoBody
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class LoginViewModel @Inject constructor(
  private val savedStateHandle: SavedStateHandle,
  private val apiRepository: ApiRepository,
  private val preferenceRepository: PreferenceRepository
) : ViewModel() {

  private val inputText = MutableStateFlow("")

  private var _uiState = MutableStateFlow(LoginUiState())
  val uiState = _uiState.asStateFlow()

  val clientId = preferenceRepository.instance.asStateFlow()
  init {
    viewModelScope.launch(Dispatchers.IO) {
      inputText
        .debounce(750)
        .filterNot(String::isEmpty)
        .mapLatest { input -> apiRepository.getServerInfo(input) }
        .buffer(0)
        .collect { serverInfo ->
          serverInfo?.let {
            _uiState.value = _uiState.value.copy(
              isTyping = false,
              instanceError = false,
              instanceTitle = it.title,
              instanceImageUrl = it.thumbnail.url,
              instanceDescription = it.description
            )
          } ?: run {
            _uiState.value = _uiState.value.copy(
              isTyping = false,
              instanceError = true
            )
          }
        }
    }
  }

  fun onValueChange(text: String) {
    inputText.update { text }
    _uiState.value = _uiState.value.copy(text = text, isTyping = true)
  }

  fun clearInputText() {
    inputText.value = ""
    _uiState.value = _uiState.value.copy(text = "", isTyping = false)
  }

  fun getClientInfo(appName: String) {
    _uiState.value = _uiState.value.copy(openDialog = true)
    viewModelScope.launch(Dispatchers.IO) {
      val result = apiRepository.getClientInfo(
        instanceName = _uiState.value.text,
        postBody = ClientInfoBody(
          clientName = appName,
          redirectUris = "mastify://oauth",
          scopes = "read write push"
        )
      )
      result?.let {
        withContext(Dispatchers.Main) {
          _uiState.value = _uiState.value.copy(openDialog = false)
          preferenceRepository.saveClientData(_uiState.value.text, it.clientId, it.clientSecret)
        }
      }
    }
  }
}

data class LoginUiState(
  val text: String = "",
  val isTyping: Boolean = false,
  val instanceError: Boolean = false,
  val instanceTitle: String = "",
  val instanceImageUrl: String = "",
  val instanceDescription: String = "",
  val openDialog: Boolean = false
)
