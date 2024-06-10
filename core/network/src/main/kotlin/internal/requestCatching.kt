package com.github.whitescent.mastify.core.network.internal

import com.github.whitescent.mastify.core.common.debug
import com.github.whitescent.mastify.core.network.NetworkResult
import com.github.whitescent.mastify.core.network.exception.NetworkConnectionException
import com.github.whitescent.mastify.core.network.exception.NetworkException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.statement.bodyAsText
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.net.UnknownServiceException

@PublishedApi
internal suspend inline fun <T> requestCatching(
  block: () -> NetworkResult<T>
) = try {
  block()
} catch (e: Throwable) {
  NetworkResult.failure<T>(e.mapNetworkException())
}

@PublishedApi
internal suspend fun Throwable.mapNetworkException(): NetworkException {
  debug("NetworkCatching", this) { "Mapping network exception" }
  // Mapping of exceptions to NetworkException
  val mapped = when (this) {
    is NetworkException -> this
    is ResponseException -> NetworkException(
      errorCode = response.status.value,
      response = runCatching { response.bodyAsText() }.getOrNull(),
      message = "inner error",
      cause = this,
    )
    is SocketException, is SocketTimeoutException -> NetworkConnectionException(
      message = "Bad connection.",
      cause = this,
    )
    is UnknownHostException -> NetworkConnectionException(
      message = "No internet connection. Please check your network settings.",
      cause = this,
    )
    is UnknownServiceException -> NetworkConnectionException(
      message = "Cannot connect to the server. Please try again later.",
      cause = this,
    )
    else -> NetworkException(
      message = "Unknown error",
      cause = this,
    )
  }
  return mapped
}
