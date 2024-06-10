package com.github.whitescent.mastify.core.network.internal

import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.serialization.json.JsonElement

internal fun jsonContentHeader() = createClientPlugin("JsonContentHeader") {
  onRequest { request, _ ->
    if (HttpHeaders.ContentType !in request.headers && request.body is JsonElement) {
      request.contentType(ContentType.Application.Json)
    }
  }
}
