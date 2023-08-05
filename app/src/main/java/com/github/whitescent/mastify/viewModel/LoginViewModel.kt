package com.github.whitescent.mastify.viewModel

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.connyduck.calladapter.networkresult.fold
import com.github.whitescent.mastify.data.model.ui.InstanceUiData
import com.github.whitescent.mastify.data.repository.LoginRepository
import com.github.whitescent.mastify.data.repository.PreferenceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class LoginViewModel @Inject constructor(
  private val preferenceRepository: PreferenceRepository,
  private val loginRepository: LoginRepository
) : ViewModel() {

  var uiState by mutableStateOf(LoginUiState())
    private set

  val instance: StateFlow<InstanceUiData?> =
    snapshotFlow { uiState.text }
      .debounce(750)
      .filterNot { it.isEmpty() || instanceLocalError }
      .map {
        loginRepository.fetchInstanceInfo(it).fold(
          onSuccess = { instance ->
            uiState = uiState.copy(isTyping = false)
            InstanceUiData(
              instance.title,
              instance.stats.userCount,
              instance.thumbnail,
              instance.shortDescription
            )
          },
          onFailure = {
            uiState = uiState.copy(isTyping = false)
            null
          }
        )
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null
      )

  val instanceLocalError by derivedStateOf {
    !loginRepository.isInstanceCorrect(uiState.text)
  }

  fun onValueChange(text: String) {
    uiState = uiState.copy(text = text, isTyping = true)
  }

  fun clearInputText() {
    uiState = uiState.copy(text = "", isTyping = false)
  }

  fun authenticateApp(appName: String, navigateToOauth: (String) -> Unit) {
    viewModelScope.launch(Dispatchers.IO) {
      loginRepository
        .authenticateApp(uiState.text, appName)
        .fold(
          onSuccess = {
            preferenceRepository.saveInstanceData(uiState.text, it.clientId, it.clientSecret)
            uiState = uiState.copy(authenticateError = false)
            withContext(Dispatchers.Main) {
              navigateToOauth(it.clientId)
            }
          },
          onFailure = {
            it.printStackTrace()
            uiState = uiState.copy(authenticateError = true)
          }
        )
    }
  }
}

data class LoginUiState(
  val text: String = "",
  val isTyping: Boolean = false,
  val authenticateError: Boolean = false
)
