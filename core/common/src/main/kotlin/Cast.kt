package com.github.whitescent.mastify.core.common

/**
 * Cast this object type to [T].
 *
 * @param T result type after conversion
 * @throws ClassCastException [T] type does not match with this object.
 */
@Throws(ClassCastException::class)
inline fun <reified T> Any?.cast(): T = this as T

/**
 * Cast this object type to [T] or return null if the type does not match.
 *
 * @param T result type after conversion
 */
inline fun <reified T> Any?.castOrNull(): T? = this as? T
