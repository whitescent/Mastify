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

package com.github.whitescent.mastify.core.data.repository

import com.github.whitescent.mastify.core.common.ioDispatcher
import com.github.whitescent.mastify.core.model.AppData
import com.github.whitescent.mastify.core.model.network.response.AccessToken
import com.github.whitescent.mastify.core.model.network.response.Account
import com.github.whitescent.mastify.core.model.network.response.AppCredentials
import com.github.whitescent.mastify.core.model.network.response.InstanceInfo
import com.github.whitescent.mastify.core.model.session.LoginSession
import com.github.whitescent.mastify.core.network.MastodonNetworkClient
import com.github.whitescent.mastify.core.network.api.MastodonApi
import com.github.whitescent.mastify.core.network.utils.isUrlCorrect
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoginRepository @Inject constructor(
  private val preferenceRepository: PreferenceRepository,
  private val networkClient: MastodonNetworkClient
) {

  var loginSession: LoginSession? = null
    private set

  fun isInstanceCorrect(instance: String): Boolean = isUrlCorrect(instance)

  fun saveLoginSession(loginSession: LoginSession) {
    this.loginSession = loginSession
  }

  fun saveAccountToken(token: String) = preferenceRepository.updateAppData(AppData(token = token))

  suspend fun fetchInstanceInfo(name: String) = withContext(ioDispatcher) {
    networkClient.get<InstanceInfo>("https://$name/${MastodonApi.fetchInstanceInfo}")
  }

  suspend fun authenticateApp(domain: String) = withContext(ioDispatcher) {
    networkClient.post<AppCredentials>(
      url = "https://$domain/${MastodonApi.authenticateApp}",
      parameters = mapOf(
        "client_name" to CLIENT_NAME,
        "redirect_uris" to REDIRECT_URIS,
        "scopes" to CLIENT_SCOPES,
        "website" to APP_WEBSITE
      ),
      formUrlEncoded = true,
    )
  }

  suspend fun fetchOAuthToken(
    domain: String,
    clientId: String,
    clientSecret: String,
    code: String
  ) = withContext(ioDispatcher) {
    networkClient.post<AccessToken>(
      url = "https://$domain/oauth/token",
      parameters = mapOf(
        "client_id" to clientId,
        "client_secret" to clientSecret,
        "code" to code,
        "grant_type" to GRANT_TYPE,
        "redirect_uri" to REDIRECT_URIS
      ),
      formUrlEncoded = true
    )
  }

  suspend fun fetchAccount() = withContext(ioDispatcher) {
    networkClient.get<Account>(
      url = "https://${loginSession!!.domain}/${MastodonApi.accountVerifyCredentials}"
    )
  }

  companion object {
    const val REDIRECT_URIS = "mastify://oauth"
    const val CLIENT_NAME = "Mastify"
    const val CLIENT_SCOPES = "read write push"
    const val APP_WEBSITE = "https://github.com/whitescent/Mastify"
    const val GRANT_TYPE = "authorization_code"
  }
}
