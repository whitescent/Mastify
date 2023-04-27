package com.github.whitescent.mastify.network.di

import android.util.Log
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.Interceptor

object NetworkClient {
  @OptIn(ExperimentalSerializationApi::class)
  val httpClient = HttpClient(OkHttp) {
    install (ContentNegotiation) {
      json(Json {
        ignoreUnknownKeys = true
        explicitNulls = false
      })
    }
    install(Logging) {
//      logger = object : Logger {
//        override fun log(message: String) {
//          Log.d("tag", message)
//        }
//      }
      logger = Logger.DEFAULT
      level = LogLevel.HEADERS
    }
    install(HttpTimeout) {
      requestTimeoutMillis = 30_000
      connectTimeoutMillis = 30_000
    }
    defaultRequest {
      contentType(ContentType.Application.Json)
      accept(ContentType.Application.Json)
    }
  }
}
