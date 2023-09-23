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

package com.github.whitescent.mastify.data.repository

import at.connyduck.calladapter.networkresult.NetworkResult
import com.github.whitescent.mastify.network.MastodonApi
import com.github.whitescent.mastify.network.model.instance.AppCredentials
import okhttp3.HttpUrl
import javax.inject.Inject

class LoginRepository @Inject constructor(private val api: MastodonApi) {

  fun isInstanceCorrect(instance: String): Boolean {
    try {
      HttpUrl.Builder().host(instance).scheme("https").build()
    } catch (e: IllegalArgumentException) {
      return false
    }
    return true
  }

  suspend fun authenticateApp(domain: String, clientName: String): NetworkResult<AppCredentials> {
    return api.authenticateApp(
      domain = domain,
      clientName = clientName,
      redirectUris = "mastify://oauth",
      scopes = "read write push",
      website = "https://github.com/whitescent/Mastify",
    )
  }
}
