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

package com.github.whitescent.mastify.data.repository

import android.content.Context
import at.connyduck.calladapter.networkresult.getOrThrow
import com.github.whitescent.mastify.network.MastodonApi
import com.github.whitescent.mastify.network.model.account.AccessToken
import com.github.whitescent.mastify.network.model.instance.AppCredentials
import com.github.whitescent.mastify.network.model.instance.InstanceInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.HttpUrl
import javax.inject.Inject

class LoginRepository @Inject constructor(
  @ApplicationContext private val context: Context,
  private val api: MastodonApi
) {

  fun isInstanceCorrect(instance: String): Boolean {
    try {
      HttpUrl.Builder().host(instance).scheme("https").build()
    } catch (e: IllegalArgumentException) {
      return false
    }
    return true
  }

  suspend fun fetchInstanceInfo(name: String): Flow<InstanceInfo> = flow {
    emit(api.fetchInstanceInfo(name).getOrThrow())
  }

  suspend fun authenticateApp(domain: String, clientName: Int): Flow<AppCredentials> = flow {
    emit(
      api.authenticateApp(
        domain = domain,
        clientName = context.getString(clientName),
        redirectUris = REDIRECT_URIS,
        scopes = CLIENT_SCOPES,
        website = APP_WEBSITE,
      ).getOrThrow()
    )
  }

  suspend fun fetchOAuthToken(
    domain: String,
    clientId: String,
    clientSecret: String,
    code: String
  ): Flow<AccessToken> = flow {
    emit(
      api.fetchOAuthToken(
        domain = domain,
        clientId = clientId,
        clientSecret = clientSecret,
        redirectUri = REDIRECT_URIS,
        code = code,
        grantType = GRANT_TYPE
      ).getOrThrow()
    )
  }

  companion object {
    const val REDIRECT_URIS = "mastify://oauth"
    const val CLIENT_SCOPES = "read write push"
    const val APP_WEBSITE = "https://github.com/whitescent/Mastify"
    const val GRANT_TYPE = "authorization_code"
  }
}
