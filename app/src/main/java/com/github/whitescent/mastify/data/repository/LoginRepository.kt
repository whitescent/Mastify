package com.github.whitescent.mastify.data.repository

import at.connyduck.calladapter.networkresult.NetworkResult
import com.github.whitescent.mastify.network.MastodonApi
import com.github.whitescent.mastify.network.model.instance.AppCredentials
import com.github.whitescent.mastify.network.model.instance.InstanceInfo
import okhttp3.HttpUrl
import javax.inject.Inject

class LoginRepository @Inject constructor(private val api: MastodonApi) {

  suspend fun fetchInstanceInfo(instance: String): NetworkResult<InstanceInfo> {
    return api.fetchInstanceInfo(instance)
  }

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
