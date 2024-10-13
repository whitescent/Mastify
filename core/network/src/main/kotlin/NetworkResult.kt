@file:Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE", "MemberVisibilityCanBePrivate")

package com.github.whitescent.mastify.core.network

import com.github.whitescent.mastify.core.network.exception.NetworkException
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@JvmInline
value class NetworkResult<out T> @PublishedApi internal constructor(
  @PublishedApi
  internal val value: Any?
) {
  inline val isSuccess: Boolean get() = !isFailure

  inline val isFailure: Boolean get() = value is NetworkException

  inline fun getOrNull(): T? = when {
    isFailure -> null
    else -> value as T
  }

  inline fun getOrThrow(): T {
    if (value is NetworkException) throw value
    return value as T
  }

  fun exceptionOrNull(): NetworkException? = when (value) {
    is NetworkException -> value
    else -> null
  }

  inline fun onFailure(action: (exception: NetworkException) -> Unit): NetworkResult<T> {
    contract { callsInPlace(action, InvocationKind.AT_MOST_ONCE) }
    exceptionOrNull()?.let { action(it) }
    return this
  }

  inline fun onSuccess(action: (value: T) -> Unit): NetworkResult<T> {
    contract { callsInPlace(action, InvocationKind.AT_MOST_ONCE) }
    if (isSuccess) action(value as T)
    return this
  }

  override fun toString(): String = when (value) {
    is NetworkException -> "NetworkResult.Failure(${value.message})"
    else -> "NetworkResult.Success($value)"
  }

  companion object {

    inline fun <T> success(value: Any?): NetworkResult<T> = NetworkResult(value)

    inline fun <T> failure(exception: NetworkException): NetworkResult<T> = NetworkResult(exception)

    @JvmName("failureAny")
    inline fun failure(exception: NetworkException): NetworkResult<Any> = NetworkResult(exception)
  }
}
