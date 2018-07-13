package com.pixelplex.echolib

import com.pixelplex.echolib.exception.LocalException

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
