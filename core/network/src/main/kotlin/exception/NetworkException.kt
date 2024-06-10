package com.github.whitescent.mastify.core.network.exception

/**
 * An exception that occurs when a network request fails.
 */
open class NetworkException(
  val errorCode: Int = -1,
  val response: String? = null,
  message: String,
  cause: Throwable? = null,
) : Exception(message, cause) {

  override val message: String get() = super.message!!

  open val isConnectionError: Boolean = false
}
