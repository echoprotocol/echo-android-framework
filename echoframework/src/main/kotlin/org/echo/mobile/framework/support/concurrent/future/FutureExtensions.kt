@file:JvmName("FutureExtensions")

package org.echo.mobile.framework.support.concurrent.future

import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.exception.LocalException
import org.echo.mobile.framework.support.Result
import org.echo.mobile.framework.support.toError
import org.echo.mobile.framework.support.toValue
import java.util.concurrent.TimeUnit

/**
 * Wraps future task result in [Result]
 *
 * Future result must be not null
 */
fun <E : Exception, T> FutureTask<T>.wrapResult(): Result<E, T> =
    try {
        val result = get()
        result!!.toValue()
    } catch (exception: Exception) {
        (exception as E).toError()
    }

/**
 * Wraps future task result in [Result] with [timeout]
 *
 * Future result must be not null
 */
fun <E : Exception, T> FutureTask<T>.wrapResult(timeout: Long, unit: TimeUnit): Result<E, T> =
    try {
        val result = get()
        result!!.toValue()
    } catch (exception: Exception) {
        (exception as E).toError()
    }

/**
 * Wraps future task result in [Result] with default value if operation succeeds with null
 */
fun <E : Exception, T> FutureTask<T>.wrapResult(default: T): Result<E, T> =
    try {
        val result = get() ?: default
        result.toValue()
    } catch (exception: Exception) {
        (exception as E).toError()
    }

/**
 * Creates [Callback] that completes future in it's methods with optional block call
 */
fun <T> FutureTask<T>.completeCallback(
    successBlock: (T) -> Unit = {},
    errorBlock: (LocalException) -> Unit = {}
): Callback<T> {
    return object : Callback<T> {
        override fun onSuccess(result: T) {
            successBlock(result)
            setComplete(result)
        }

        override fun onError(error: LocalException) {
            errorBlock(error)
            setComplete(error)
        }

    }
}

