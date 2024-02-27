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

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.whitescent.mastify.data.repository.AccountRepository
import com.github.whitescent.mastify.data.repository.InstanceRepository
import com.github.whitescent.mastify.data.repository.LoginRepository
import com.github.whitescent.mastify.data.repository.PreferenceRepository
import com.github.whitescent.mastify.mapper.toEntity
import com.github.whitescent.mastify.network.model.account.AccessToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OauthViewModel @Inject constructor(
  savedStateHandle: SavedStateHandle,
  preferenceRepository: PreferenceRepository,
  private val instanceRepository: InstanceRepository,
  private val loginRepository: LoginRepository,
  private val accountRepository: AccountRepository
) : ViewModel() {

  val instance = preferenceRepository.instance
  val code: String? = savedStateHandle["code"]

  private val navigateChannel = Channel<Unit>()
  val navigateFlow = navigateChannel.receiveAsFlow()

  fun fetchAccessToken() {
    val domain = instance!!.name
    val clientId = instance.clientId
    val clientSecret = instance.clientSecret

    viewModelScope.launch(Dispatchers.IO) {
      loginRepository.fetchOAuthToken(
        domain = domain,
        clientId = clientId,
        clientSecret = clientSecret,
        code = code!!
      ).catch { it.printStackTrace() }
        .collect {
          fetchAccountDetails(it, domain, clientId, clientSecret)
          navigateChannel.send(Unit)
        }
    }
  }

  private suspend fun fetchAccountDetails(
    accessToken: AccessToken,
    domain: String,
    clientId: String,
    clientSecret: String
  ) {
    accountRepository.fetchAccountVerifyCredentials(domain, accessToken.accessToken)
      .catch { it.printStackTrace() }
      .collect {
        accountRepository.addAccount(
          it.toEntity(
            accessToken = accessToken.accessToken,
            clientId = clientId,
            clientSecret = clientSecret,
            isActive = true,
            accountId = it.id,
            id = 0,
            firstVisibleItemIndex = 0,
            offset = 0,
            lastNotificationId = null,
          )
        )
        instanceRepository.upsertInstanceInfo()
        instanceRepository.upsertEmojis()
      }
  }
}
