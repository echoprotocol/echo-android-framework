@file:JvmName("FutureExtensions")

package com.pixelplex.echoframework.support.concurrent.future

import com.pixelplex.echoframework.support.Result
import com.pixelplex.echoframework.support.toError
import com.pixelplex.echoframework.support.toValue

/**
 * Wraps future task result in [Result]
 *
 * <p>
 *     Future result must be not null
 * </p>
 */
fun <E : Exception, T> FutureTask<T>.wrapResult(): Result<E, T> =
    try {
        val result = get()
        toValue(result!!)
    } catch (exception: Exception) {
        toError(exception as E)
    }

/**
 * Wraps future task result in [Result] with default value if operation succeeds with null
 */
fun <E : Exception, T> FutureTask<T>.wrapResult(default: T): Result<E, T> =
    try {
        val result = get() ?: default
        toValue(result)
    } catch (exception: Exception) {
        toError(exception as E)
    }
