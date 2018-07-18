@file:JvmName("FutureExtensions")

package com.pixelplex.echolib.support

/**
 * Encapsulates operation results
 *
 * <p>
 *     Provides abstraction over success\failure operation results
 * </p>
 */
sealed class Result<out V, out E : Exception>

/**
 * Success operation result
 */
class Success<out V, out E : Exception>(val result: V) : Result<V, E>()

/**
 * Error operation result
 */
class Failure<out V, out E : Exception>(val error: E) : Result<V, E>()

/**
 * Breaks result on error\success and provides functional way to work with them
 */
inline fun <V, E : Exception, T> Result<V, E>.fold(success: (V) -> T, failure: (E) -> T) {
    when (this) {
        is Success -> success(result)
        is Failure -> failure(error)
    }
}
