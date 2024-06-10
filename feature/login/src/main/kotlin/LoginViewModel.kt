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

package com.github.whitescent.mastify.feature.login

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.whitescent.mastify.core.common.debug
import com.github.whitescent.mastify.core.data.repository.LoginRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
  savedStateHandle: SavedStateHandle,
  private val loginRepository: LoginRepository
) : ViewModel() {

  private val code: String? = savedStateHandle["code"]

  var uiState by mutableStateOf(LoginUiState())
    private set

  val loginInput by mutableStateOf(TextFieldState())

  val instanceLocalError by derivedStateOf {
    !loginRepository.isInstanceCorrect(loginInput.text.toString())
  }

  fun checkInstance() {
    viewModelScope.launch {
      uiState = uiState.copy(loginStatus = LoginStatus.Loading)
      loginRepository.fetchInstanceInfo(loginInput.text.toString())
        .onFailure {
          uiState = uiState.copy(loginStatus = LoginStatus.Failure)
        }
        .onSuccess {
          authenticateApp()
        }
    }
  }

  private fun authenticateApp() {
    viewModelScope.launch {
      loginRepository.authenticateApp(loginInput.text.toString())
        .onFailure {
          uiState = uiState.copy(
            authenticateError = true,
            loginStatus = LoginStatus.Idle
          )
        }
        .onSuccess {
          debug { "credentials is $it" }
//          preferenceRepository.saveInstanceData(
//            domain = loginInput.text.toString(),
//            clientId = it.clientId,
//            clientSecret = it.clientSecret
//          )
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
  val authenticateError: Boolean = false,
  val loginStatus: LoginStatus = LoginStatus.Idle,
  val clientId: String = ""
)

sealed class LoginStatus {
  data object Idle : LoginStatus()
  data object Loading : LoginStatus()
  data object Failure : LoginStatus()
}
