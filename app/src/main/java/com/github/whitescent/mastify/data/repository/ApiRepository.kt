package com.github.whitescent.mastify.data.repository

import com.github.whitescent.mastify.network.di.NetworkClient
import com.github.whitescent.mastify.network.model.request.ClientInfoBody
import com.github.whitescent.mastify.network.model.request.OauthTokenBody
import com.github.whitescent.mastify.network.model.response.instance.ClientInfo
import com.github.whitescent.mastify.network.model.response.instance.InstanceInfo
import com.github.whitescent.mastify.network.model.response.account.Token
import com.github.whitescent.mastify.network.model.response.account.Profile
import com.github.whitescent.mastify.network.model.response.account.Status
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiRepository @Inject constructor() {
  suspend fun getServerInfo(instanceName: String) =
    runCatching {
      NetworkClient.httpClient.get("https://$instanceName/api/v2/instance").body<InstanceInfo>()
    }.getOrNull()

  suspend fun getClientInfo(instanceName: String, postBody: ClientInfoBody) =
    runCatching {
      NetworkClient.httpClient.post("https://$instanceName/api/v1/apps") {
        contentType(ContentType.Application.Json)
        setBody(postBody)
      }.body<ClientInfo>()
    }.getOrNull()

  suspend fun getAccessToken(instanceName: String, postBody: OauthTokenBody) =
    runCatching {
      NetworkClient.httpClient.post("https://$instanceName/oauth/token") {
        contentType(ContentType.Application.Json)
        setBody(postBody)
      }.body<Token>()
    }.getOrNull()

  suspend fun getProfile(instanceName: String, token: String) = withContext(Dispatchers.IO) {
    runCatching {
      NetworkClient.httpClient
        .get("https://$instanceName/api/v1/accounts/verify_credentials") {
          header("Authorization", "Bearer $token")
        }.body<Profile>()
    }.getOrNull()
  }

  suspend fun getAccountStatuses(instanceName: String, token: String, id: String) =
    withContext(Dispatchers.IO) {
      runCatching {
        NetworkClient.httpClient
          .get("https://$instanceName/api/v1/accounts/$id/statuses") {
            header("Authorization", "Bearer $token")
          }.body<List<Status>>()
      }.getOrNull()
    }

  suspend fun getHomeTimeline(
    instanceName:String,
    token: String,
    maxId: String? = null
  ) =
    NetworkClient.httpClient.get("https://$instanceName/api/v1/timelines/home") {
      header("Authorization", "Bearer $token")
      maxId?.let {
        url {
          parameters.append("max_id", maxId)
        }
      }
    }.body<List<Status>>()
}
