package com.github.whitescent.mastify.viewModel

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import at.connyduck.calladapter.networkresult.fold
import com.github.whitescent.mastify.data.repository.LoginRepository
import com.github.whitescent.mastify.data.repository.PreferenceRepository
import com.github.whitescent.mastify.network.MastodonApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class LoginViewModel @Inject constructor(
  private val preferenceRepository: PreferenceRepository,
  private val loginRepository: LoginRepository,
  private val api: MastodonApi
) : ViewModel() {

  var uiState by mutableStateOf(LoginUiState())
    private set

  val instanceLocalError by derivedStateOf {
    !loginRepository.isInstanceCorrect(uiState.text)
  }

  fun onValueChange(text: String) {
    uiState = uiState.copy(text = text)
  }

  fun clearInputText() {
    uiState = uiState.copy(text = "")
  }

  fun checkInstance(navigateToOauth: (String) -> Unit) {
    viewModelScope.launch {
      uiState = uiState.copy(loginStatus = LoginStatus.Loading)
      api.fetchInstanceInfo(uiState.text).fold(
        onSuccess = { instance ->
          authenticateApp(instance.title, navigateToOauth)
        },
        onFailure = {
          uiState = uiState.copy(loginStatus = LoginStatus.Failure)
        }
      )
    }
  }

  private fun authenticateApp(appName: String, navigateToOauth: (String) -> Unit) {
    viewModelScope.launch(Dispatchers.IO) {
      loginRepository.authenticateApp(uiState.text, appName)
        .fold(
          onSuccess = {
            preferenceRepository.saveInstanceData(uiState.text, it.clientId, it.clientSecret)
            uiState = uiState.copy(authenticateError = false)
            withContext(Dispatchers.Main) {
              navigateToOauth(it.clientId)
              uiState = uiState.copy(loginStatus = LoginStatus.Idle)
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
  val authenticateError: Boolean = false,
  val loginStatus: LoginStatus = LoginStatus.Idle
)

sealed class LoginStatus {
  data object Idle : LoginStatus()
  data object Loading : LoginStatus()
  data object Failure : LoginStatus()
}
