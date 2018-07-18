package com.pixelplex.echolib.support.concurrent.future

import com.pixelplex.echolib.support.Failure
import com.pixelplex.echolib.support.Result
import com.pixelplex.echolib.support.Success

/**
 * Wraps future task result in [Result]
 *
 * <p>
 *     Future result must be not null
 * </p>
 */
fun <T, E : Exception> FutureTask<T>.wrapResult(): Result<T, E> =
    try {
        val result = get()
        Success(result!!)
    } catch (exception: Exception) {
        Failure(exception as E)
    }

/**
 * Wraps future task result in [Result] with default value if operation succeeds with null
 */
fun <T, E : Exception> FutureTask<T>.wrapResult(default: T): Result<T, E> =
    try {
        val result = get()
        Success(result ?: default)
    } catch (exception: Exception) {
        Failure(exception as E)
    }
