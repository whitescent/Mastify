/*
 * Copyright 2023 WhiteScent
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
import at.connyduck.calladapter.networkresult.fold
import com.github.whitescent.mastify.data.repository.AccountRepository
import com.github.whitescent.mastify.data.repository.InstanceRepository
import com.github.whitescent.mastify.data.repository.PreferenceRepository
import com.github.whitescent.mastify.network.MastodonApi
import com.github.whitescent.mastify.network.model.account.AccessToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class OauthViewModel @Inject constructor(
  savedStateHandle: SavedStateHandle,
  preferenceRepository: PreferenceRepository,
  private val instanceRepository: InstanceRepository,
  private val api: MastodonApi,
  private val accountRepository: AccountRepository
) : ViewModel() {

  val instance = preferenceRepository.instance
  val code: String? = savedStateHandle["code"]

  fun fetchAccessToken(navigateToApp: () -> Unit) {
    val domain = instance!!.name
    val clientId = instance.clientId
    val clientSecret = instance.clientSecret

    viewModelScope.launch(Dispatchers.IO) {
      api.fetchOAuthToken(
        domain = domain,
        clientId = clientId,
        clientSecret = clientSecret,
        redirectUri = "mastify://oauth",
        code = code!!,
        grantType = "authorization_code"
      ).fold(
        { accessToken ->
          fetchAccountDetails(accessToken, domain, clientId, clientSecret)
          withContext(Dispatchers.Main) {
            navigateToApp()
          }
        },
        {
          it.printStackTrace()
        }
      )
    }
  }

  private suspend fun fetchAccountDetails(
    accessToken: AccessToken,
    domain: String,
    clientId: String,
    clientSecret: String
  ) {
    api.accountVerifyCredentials(
      domain = domain,
      auth = "Bearer ${accessToken.accessToken}"
    ).fold(
      { newAccount ->
        accountRepository.addAccount(
          accessToken = accessToken.accessToken,
          domain = domain,
          clientId = clientId,
          clientSecret = clientSecret,
          newAccount = newAccount
        )
        instanceRepository.getAndUpdateInstanceInfo()
        instanceRepository.getEmojis()
      },
      {
        it.printStackTrace()
      }
    )
  }
}
