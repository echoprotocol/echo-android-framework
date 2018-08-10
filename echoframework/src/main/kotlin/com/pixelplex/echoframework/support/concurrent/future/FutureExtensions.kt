@file:JvmName("FutureExtensions")

package com.pixelplex.echoframework.support.concurrent.future

import com.pixelplex.echoframework.Callback
import com.pixelplex.echoframework.exception.LocalException
import com.pixelplex.echoframework.support.Result
import com.pixelplex.echoframework.support.toError
import com.pixelplex.echoframework.support.toValue

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

