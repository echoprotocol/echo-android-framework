package org.echo.mobile.framework.support.concurrent

import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.exception.LocalException

/**
 * Executes callback events on main (UI) thread
 *
 * @author Dmitriy Bushuev
 */
class MainThreadCallback<T>(private val delegate: Callback<T>) : Callback<T> {

    private val mainThreadExecutor = MainThreadExecutor()

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
