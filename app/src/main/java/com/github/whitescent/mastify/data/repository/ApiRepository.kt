package com.github.whitescent.mastify.data.repository

import com.github.whitescent.mastify.network.di.NetworkClient
import com.github.whitescent.mastify.network.model.request.ClientInfoBody
import com.github.whitescent.mastify.network.model.request.OauthTokenBody
import com.github.whitescent.mastify.network.model.response.instance.ClientInfo
import com.github.whitescent.mastify.network.model.response.instance.ServerInfo
import com.github.whitescent.mastify.network.model.response.instance.Token
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiRepository @Inject constructor() {
  suspend fun getServerInfo(instanceName: String) =
    runCatching {
      NetworkClient.httpClient.get("https://$instanceName/api/v2/instance").body<ServerInfo>()
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

}
