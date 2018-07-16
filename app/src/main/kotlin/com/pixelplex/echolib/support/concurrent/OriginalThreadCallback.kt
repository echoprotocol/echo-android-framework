package com.pixelplex.echolib.support.concurrent

import com.pixelplex.echolib.Callback
import com.pixelplex.echolib.exception.LocalException

/**
 * Executes callback events on thread, where this callback was originally created
 *
 * <p>
 *     Should be initialized before starting asynchronous request
 * </p>
 *
 * @author Dmitriy Bushuev
 */
class OriginalThreadCallback<T>(private val delegate: Callback<T>) : Callback<T> {

    private val originalThreadExecutor =
        OriginalThreadExecutor()

    override fun onSuccess(result: T) {
        originalThreadExecutor.execute {
            delegate.onSuccess(result)
        }
    }

    override fun onError(error: LocalException) {
        originalThreadExecutor.execute {
            delegate.onError(error)
        }
    }

}
