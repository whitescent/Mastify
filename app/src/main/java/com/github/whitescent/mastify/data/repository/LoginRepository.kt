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
