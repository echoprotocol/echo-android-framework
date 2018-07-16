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
class MainThreadCallback<T>(private val delegate: Callback<T>) : Callback<T> {

    private val mainThreadExecutor =
        MainThreadExecutor()

    override fun onSuccess(result: T) {
        mainThreadExecutor.execute {
            delegate.onSuccess(result)
        }
    }

    override fun onError(error: LocalException) {
        mainThreadExecutor.execute {
            delegate.onError(error)
        }
    }

}
