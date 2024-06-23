package com.github.whitescent.mastify.core.network

import com.github.whitescent.mastify.core.network.internal.requestCatching
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.withCharset
import io.ktor.util.reflect.typeInfo

class MastodonNetworkClient(
  @PublishedApi
  internal val inner: HttpClient
) {

  suspend inline fun <reified T> get(
    url: String,
    headers: Map<Any, Any?> = emptyMap(),
  ): NetworkResult<T> = requestCatching {
    val type = typeInfo<NetworkResult<T>>()
    inner.get(url) {
      headers {
        headers.forEach {
          append(it.key.toString(), it.value.toString())
        }
      }
    }.body(type)
  }

  suspend inline fun <reified T> get(
    url: String,
    headers: Map<Any, Any?> = emptyMap(),
    query: Map<Any, Any?> = emptyMap(),
  ): NetworkResult<T> = requestCatching {
    val type = typeInfo<NetworkResult<T>>()
    inner.get(url) {
      url {
        headers {
          headers.forEach {
            append(it.key.toString(), it.value.toString())
          }
        }
        query.forEach {
          encodedParameters.append(it.key.toString(), it.value.toString())
        }
      }
    }.body(type)
  }

  suspend inline fun <reified T> post(
    url: String,
    parameters: Map<Any, Any?> = emptyMap(),
    formUrlEncoded: Boolean = false,
    bodyConfiguration: BodyConfiguration.() -> Unit = {}
  ): NetworkResult<T> = requestCatching {
    val type = typeInfo<NetworkResult<T>>()
    val configuration = BodyConfiguration(
      requestBuilder = HttpRequestBuilder().apply {
        when (formUrlEncoded) {
          true -> contentType(ContentType.Application.FormUrlEncoded.withCharset(Charsets.UTF_8))
          false -> contentType(ContentType.Application.Json)
        }
      }
    ).buildRequest(url).apply(bodyConfiguration).apply {
      requestBuilder.url.parameters.apply {
        parameters.forEach { (key, value) ->
          if (value != null) append(key.toString(), value.toString())
        }
      }
    }
    inner.post(configuration.requestBuilder).body(type)
  }

  @JvmInline
  value class BodyConfiguration(
    @PublishedApi
    internal val requestBuilder: HttpRequestBuilder = HttpRequestBuilder().apply {
      contentType(ContentType.Application.Json)
    }
  ) {
    @PublishedApi
    internal fun buildRequest(url: String): BodyConfiguration {
      requestBuilder.url(url)
      return this
    }

    inline fun <reified T> body(data: T) = requestBuilder.setBody(data)
  }
}
