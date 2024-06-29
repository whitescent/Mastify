package com.github.whitescent.mastify.core.common

import kotlinx.coroutines.Dispatchers

/**
 * Provides a dispatcher for the main/UI thread.
 *
 * This dispatcher is used to run a coroutine on the main Android thread.
 * This should be used only for interacting with the UI and performing quick work.
 * Examples include calling suspend functions, running Android UI framework operations, and
 * updating LiveData objects.
 */
inline val uiDispatcher get() = Dispatchers.Main.immediate

/**
 * Provides a dispatcher for I/O operations.
 *
 * This dispatcher is optimized to perform disk or network I/O outside of the main thread.
 * Examples include using the Room component, reading from or writing to files, and running any
 * network operations.
 */
inline val ioDispatcher get() = Dispatchers.IO

/**
 * Provides a dispatcher for computationally intensive tasks.
 *
 * This dispatcher is optimized to perform CPU-intensive work outside of the main thread.
 * Example use cases include sorting a list and parsing JSON.
 */
inline val computationDispatcher get() = Dispatchers.Default
