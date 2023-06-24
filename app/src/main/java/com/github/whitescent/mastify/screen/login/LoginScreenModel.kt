package com.github.whitescent.mastify.screen.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.connyduck.calladapter.networkresult.fold
import com.github.whitescent.mastify.data.repository.PreferenceRepository
import com.github.whitescent.mastify.network.MastodonApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class LoginViewModel @Inject constructor(
  private val api: MastodonApi,
  private val preferenceRepository: PreferenceRepository
) : ViewModel() {

  private val inputText = MutableStateFlow("")

  var uiState by mutableStateOf(LoginUiState())
    private set

  init {
    viewModelScope.launch(Dispatchers.IO) {
      inputText
        .debounce(750)
        .filterNot(String::isEmpty)
        .mapLatest { input -> api.fetchInstanceInfo(input) }
        .buffer(0)
        .collect { apiResult ->
          apiResult.fold(
            { instance ->
              uiState = uiState.copy(
                isTyping = false,
                instanceError = false,
                instanceTitle = instance.title,
                activeMonth = instance.usage.users.activeMonth,
                instanceImageUrl = instance.thumbnail.url,
                instanceDescription = instance.description
              )
            },
            {
              uiState = uiState.copy(
                isTyping = false,
                instanceError = true
              )
            }
          )
        }
    }
  }

  fun onValueChange(text: String) {
    inputText.update { text }
    uiState = uiState.copy(text = text, isTyping = true)
  }

  fun clearInputText() {
    inputText.update { "" }
    uiState = uiState.copy(text = "", isTyping = false)
  }

  fun authenticateApp(appName: String, navigateToOauth: (String) -> Unit) {
    viewModelScope.launch(Dispatchers.IO) {
      api.authenticateApp(
        domain = uiState.text,
        clientName = appName,
        redirectUris = "mastify://oauth",
        scopes = "read write push",
        website = "https://github.com/whitescent/Mastify",
      ).fold(
        {
          preferenceRepository.saveInstanceData(
            uiState.text,
            it.clientId,
            it.clientSecret
          )
          withContext(Dispatchers.Main) {
            uiState = uiState.copy(authenticateError = false)
            navigateToOauth(it.clientId)
          }
        },
        {
          uiState = uiState.copy(authenticateError = true)
        }
      )
    }
  }
}

data class LoginUiState(
  val text: String = "",
  val isTyping: Boolean = false,
  val instanceError: Boolean = false,
  val instanceTitle: String = "",
  val activeMonth: Int = 0,
  val instanceImageUrl: String = "",
  val instanceDescription: String = "",
  val authenticateError: Boolean = false
)
