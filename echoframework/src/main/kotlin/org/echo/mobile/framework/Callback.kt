package org.echo.mobile.framework

import org.echo.mobile.framework.exception.LocalException
import org.echo.mobile.framework.support.Result
import org.echo.mobile.framework.support.error
import org.echo.mobile.framework.support.value

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
