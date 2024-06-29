package com.github.whitescent.mastify.core.network.exception

/**
 * An exception that occurs when a network request fails due to a connection error.
 */
class NetworkConnectionException(
  response: String? = null,
  message: String,
  cause: Throwable? = null
) : NetworkException(response = response, message = message, cause = cause) {
  override val isConnectionError: Boolean = true
}
