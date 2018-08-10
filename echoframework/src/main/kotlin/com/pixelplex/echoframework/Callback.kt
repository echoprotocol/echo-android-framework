package com.pixelplex.echoframework

import com.pixelplex.echoframework.exception.LocalException
import com.pixelplex.echoframework.support.Result
import com.pixelplex.echoframework.support.error
import com.pixelplex.echoframework.support.value

/**
 * Generic callback handler
 *
 * @author Dmitriy Bushuev
 */
interface Callback<T> {

    /**
     * Calls when operation is successful
     *
     * @param result Result of operation
     */
    fun onSuccess(result: T)

    /**
     * Calls when operation is failed
     *
     * @param error Error occurred during operation process
     */
    fun onError(error: LocalException)

}

/**
 * Wrap input code block in [Result] and and calls corresponding callback methods after finish
 */
fun <T> Callback<T>.processResult(block: () -> T) {
    Result { block() }
        .value { result -> this.onSuccess(result) }
        .error { error -> this.onError(LocalException(error)) }
}

/**
 * Calls corresponding callback methods on [targetResult] processing results
 */
fun <E : Exception, T> Callback<T>.processResult(targetResult: Result<E, T>) {
    targetResult
        .value { result -> this.onSuccess(result) }
        .error { error -> this.onError(LocalException(error)) }
}
