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
import com.github.whitescent.mastify.core.model.network.response.AppCredentials
import com.github.whitescent.mastify.core.model.network.response.InstanceInfo
import com.github.whitescent.mastify.core.network.MastodonNetworkClient
import com.github.whitescent.mastify.core.network.api.MastodonApi
import com.github.whitescent.mastify.core.network.utils.isUrlCorrect
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LoginRepository @Inject constructor(
  private val networkClient: MastodonNetworkClient
) {

  fun isInstanceCorrect(instance: String): Boolean = isUrlCorrect(instance)

  suspend fun fetchInstanceInfo(name: String) = withContext(ioDispatcher) {
    networkClient.get<InstanceInfo>("https://$name/${MastodonApi.fetchInstanceInfo}")
  }

  suspend fun authenticateApp(domain: String) = withContext(ioDispatcher) {
    networkClient.form<AppCredentials>("https://$domain/${MastodonApi.authenticateApp}") {
      buildParameters {
        append("client_name", CLIENT_NAME)
        append("redirect_uris", REDIRECT_URIS)
        append("scopes", CLIENT_SCOPES)
        append("website", APP_WEBSITE)
      }
    }
  }

  companion object {
    const val REDIRECT_URIS = "mastify://oauth"
    const val CLIENT_NAME = "Mastify"
    const val CLIENT_SCOPES = "read write push"
    const val APP_WEBSITE = "https://github.com/whitescent/Mastify"
    const val GRANT_TYPE = "authorization_code"
  }
}
