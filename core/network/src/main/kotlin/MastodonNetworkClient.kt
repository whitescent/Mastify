package com.github.whitescent.mastify.core.network

import com.github.whitescent.mastify.core.common.debug
import com.github.whitescent.mastify.core.network.internal.requestCatching
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.Parameters
import io.ktor.http.contentType
import io.ktor.http.parameters
import io.ktor.http.withCharset
import io.ktor.util.reflect.typeInfo

class MastodonNetworkClient(
  @PublishedApi
  internal val inner: HttpClient
) {

  suspend inline fun <reified T> get(
    url: String,
    headers: List<Pair<Any, Any>> = emptyList(),
  ): NetworkResult<T> = requestCatching {
    val type = typeInfo<NetworkResult<T>>()
    inner.get(url) {
      headers {
        headers.forEach {
          append(it.first.toString(), it.second.toString())
        }
      }
    }.body(type)
  }

  suspend inline fun <reified T> get(
    url: String,
    headers: List<Pair<Any, Any>> = emptyList(),
    query: List<Pair<Any, Any>> = emptyList()
  ): NetworkResult<T> = requestCatching {
    val type = typeInfo<NetworkResult<T>>()
    inner.get(url) {
      url {
        headers {
          headers.forEach {
            append(it.first.toString(), it.second.toString())
          }
        }
        query.forEach {
          encodedParameters.append(it.first.toString(), it.second.toString())
        }
      }
    }.body(type)
  }

  suspend inline fun <reified T> post(
    url: String,
    bodyConfiguration: BodyConfiguration.() -> Unit
  ): NetworkResult<T> = requestCatching {
    val type = typeInfo<NetworkResult<T>>()
    val configuration = BodyConfiguration().url(url).apply(bodyConfiguration)
    inner.post(configuration.requestBuilder).body(type)
  }

  suspend inline fun <reified T> form(
    url: String,
    bodyConfiguration: BodyConfiguration.() -> Unit
  ): NetworkResult<T> = requestCatching {
    val type = typeInfo<NetworkResult<T>>()
    val configuration = BodyConfiguration(
      requestBuilder = HttpRequestBuilder().apply {
        contentType(ContentType.Application.FormUrlEncoded.withCharset(Charsets.UTF_8))
      }
    ).url(url).apply(bodyConfiguration)
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
    internal fun url(url: String): BodyConfiguration {
      requestBuilder.apply {
        url(url)
      }
      return this
    }

    inline fun <reified T> body(data: T) = requestBuilder.setBody(data)

    inline fun buildParameters(crossinline builder: ParametersBuilder.() -> Unit) {
      requestBuilder.apply {
        val parametersBuilder = ParametersBuilder()
        body(
          FormDataContent(parametersBuilder.buildParameters { builder(this) })
        )
      }
    }

    class ParametersBuilder {
      private val parameters = mutableListOf<Pair<String, String>>()

      fun append(name: String, value: String) {
        parameters.add(Pair(name, value))
      }

      @PublishedApi
      internal fun buildParameters(builder: ParametersBuilder.() -> Unit): Parameters {
        builder()
        val result = parameters {
          parameters.forEach { append(it.first, it.second) }
        }
        debug { "result parameters is $result" }
        return result
      }
    }
  }
}
