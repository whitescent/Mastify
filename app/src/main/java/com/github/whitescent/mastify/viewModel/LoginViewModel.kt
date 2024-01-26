/*
 * Copyright 2024 WhiteScent
 *
 * This file is a part of Mastify.
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * Mastify is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Mastify; if not,
 * see <http://www.gnu.org/licenses>.
 */

package com.github.whitescent.mastify.viewModel

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.whitescent.R
import com.github.whitescent.mastify.data.repository.LoginRepository
import com.github.whitescent.mastify.data.repository.PreferenceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
  private val preferenceRepository: PreferenceRepository,
  private val loginRepository: LoginRepository
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

  fun checkInstance() {
    viewModelScope.launch {
      uiState = uiState.copy(loginStatus = LoginStatus.Loading)
      loginRepository.fetchInstanceInfo(uiState.text)
        .catch {
          uiState = uiState.copy(loginStatus = LoginStatus.Failure)
        }
        .collect {
          authenticateApp()
        }
    }
  }

  private fun authenticateApp() {
    viewModelScope.launch(Dispatchers.IO) {
      loginRepository.authenticateApp(uiState.text, R.string.app_name)
        .catch {
          it.printStackTrace()
          uiState = uiState.copy(authenticateError = true)
        }
        .collect {
          preferenceRepository.saveInstanceData(uiState.text, it.clientId, it.clientSecret)
          uiState = uiState.copy(
            authenticateError = false,
            loginStatus = LoginStatus.Idle,
            clientId = it.clientId
          )
        }
    }
  }
}

data class LoginUiState(
  val text: String = "",
  val authenticateError: Boolean = false,
  val loginStatus: LoginStatus = LoginStatus.Idle,
  val clientId: String = ""
)

sealed class LoginStatus {
  data object Idle : LoginStatus()
  data object Loading : LoginStatus()
  data object Failure : LoginStatus()
}
