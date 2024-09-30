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
import com.github.whitescent.mastify.core.model.session.LoginSession
import com.github.whitescent.mastify.core.network.api.MastodonApi
import com.github.whitescent.mastify.core.network.utils.isUrlCorrect
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoginRepository @Inject constructor(
  private val preferenceRepository: PreferenceRepository,
  private val api: MastodonApi
) {

  var loginSession: LoginSession? = null
    private set

  val appData = preferenceRepository.getAppData()

  fun isInstanceCorrect(instance: String): Boolean = isUrlCorrect(instance)

  fun updateAppData(appData: AppData) = preferenceRepository.updateAppData(appData)

  fun saveLoginSession(session: LoginSession) { loginSession = session }

  suspend fun fetchInstanceInfo(domain: String) = withContext(ioDispatcher) {
    api.fetchInstanceInfo("https://$domain/api/v1/instance")
  }

  suspend fun authenticateApp(domain: String) = withContext(ioDispatcher) {
    api.authenticateApp(
      url = "https://$domain/api/v1/apps",
      clientName = CLIENT_NAME,
      redirectUris = REDIRECT_URIS,
      scopes = CLIENT_SCOPES,
      website = APP_WEBSITE
    )
  }

  suspend fun fetchOAuthToken(
    domain: String,
    clientId: String,
    clientSecret: String,
    code: String
  ) = withContext(ioDispatcher) {
    api.fetchOAuthToken(
      url = "https://$domain/oauth/token",
      clientId = clientId,
      clientSecret = clientSecret,
      code = code,
      grantType = GRANT_TYPE,
      redirectUri = REDIRECT_URIS
    )
  }

  suspend fun fetchAccount() = withContext(ioDispatcher) {
    api.accountVerifyCredentials("https://${loginSession!!.domain}/api/v1/accounts/verify_credentials")
  }

  private companion object {
    const val REDIRECT_URIS = "mastify://oauth"
    const val CLIENT_NAME = "Mastify"
    const val CLIENT_SCOPES = "read write push"
    const val APP_WEBSITE = "https://github.com/whitescent/Mastify"
    const val GRANT_TYPE = "authorization_code"
  }
}
