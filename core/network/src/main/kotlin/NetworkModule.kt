package com.github.whitescent.mastify.core.network

import com.github.whitescent.mastify.core.common.debug
import com.github.whitescent.mastify.core.model.AppDataProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.MessageLengthLimitingLogger
import io.ktor.client.request.bearerAuth
import io.ktor.http.HttpHeaders
import io.ktor.http.URLProtocol
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {
  @Provides
  @Singleton
  fun provideHttpClient(
    json: Json
  ) = HttpClient(OkHttp) {
    defaultRequest {
      headers.append(HttpHeaders.UserAgent, "Mastify-Android")
    }
    install(networkInterceptor(json))
    install(ContentNegotiation) { json(json) }
    install(HttpTimeout) {
      requestTimeoutMillis = 60.seconds.toLong(DurationUnit.MILLISECONDS)
      socketTimeoutMillis = 30.seconds.toLong(DurationUnit.MILLISECONDS)
    }
    install(HttpRequestRetry) {
      noRetry()
      exponentialDelay()
    }
    if (BuildConfig.DEBUG) install(Logging) {
      level = LogLevel.ALL
      logger = MessageLengthLimitingLogger(
        delegate = object : Logger {
          override fun log(message: String) = debug("HttpClient") { message }
        }
      )
      sanitizeHeader { header -> header == HttpHeaders.Authorization }
    }
  }

  @Provides
  @Singleton
  fun provideMastodonNetworkClient(
    httpClient: HttpClient,
    appData: AppDataProvider,
  ): MastodonNetworkClient = MastodonNetworkClient(
    httpClient.config {
      defaultRequest {
        val data = appData.getAppData()
        url.protocol = URLProtocol.HTTPS
        data.instanceUrl?.let {
          url.host = data.instanceUrl!!
        }
        data.token?.let {
          bearerAuth(it)
        }
      }
    }
  )
}
