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
import com.github.whitescent.mastify.core.model.network.response.Account
import com.github.whitescent.mastify.core.model.session.LoginSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
  savedStateHandle: SavedStateHandle,
  private val repository: LoginRepository
) : ViewModel() {

  val code: String? = savedStateHandle["code"]

  init {
    if (code != null) {
      val loginSession = repository.loginSession
      if (loginSession != null) fetchAccessToken()
    }
  }

  var uiState by mutableStateOf(LoginUiState())
    private set

  val loginInput by mutableStateOf(TextFieldState())

  val instanceLocalError by derivedStateOf {
    !repository.isInstanceCorrect(loginInput.text.toString())
  }

  fun checkInstance() {
    viewModelScope.launch {
      uiState = uiState.copy(loginStatus = LoginStatus.Loading)
      repository.fetchInstanceInfo(loginInput.text.toString())
        .onFailure { uiState = uiState.copy(loginStatus = LoginStatus.Failure) }
        .onSuccess { authenticateApp() }
    }
  }

  private fun authenticateApp() {
    viewModelScope.launch {
      repository.authenticateApp(loginInput.text.toString())
        .onFailure {
          uiState = uiState.copy(
            authenticateError = true,
            loginStatus = LoginStatus.Idle
          )
        }
        .onSuccess { credentials ->
          repository.saveLoginSession(
            loginSession = LoginSession(
              clientId = credentials.clientId,
              clientSecret = credentials.clientSecret,
              domain = loginInput.text.toString()
            )
          )
          uiState = uiState.copy(
            authenticateError = false,
            loginStatus = LoginStatus.Idle,
            clientId = credentials.clientId
          )
        }
    }
  }

  private fun fetchAccessToken() = viewModelScope.launch {
    repository.fetchOAuthToken(
      domain = repository.loginSession!!.domain,
      clientId = repository.loginSession!!.clientId,
      clientSecret = repository.loginSession!!.clientSecret,
      code = code!!
    ).onSuccess {
      repository.saveAccountToken(it.accessToken)
      fetchAccount()
    }.onFailure { }
  }

  private fun fetchAccount() = viewModelScope.launch {
    repository.fetchAccount()
      .onSuccess {
        uiState = uiState.copy(fetchedAccount = it)
      }
      .onFailure {
        debug(it) { "fetch account failed" }
      }
  }
}

data class LoginUiState(
  val authenticateError: Boolean = false,
  val loginStatus: LoginStatus = LoginStatus.Idle,
  val clientId: String = "",
  val fetchedAccount: Account? = null
)

sealed class LoginStatus {
  data object Idle : LoginStatus()
  data object Loading : LoginStatus()
  data object Failure : LoginStatus()
}
