package org.echo.mobile.framework.support

import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.exception.LocalException

/**
 * Empty instance of operation callback
 *
 * Useful when passing callback is unnecessary
 *
 * @author Dmitriy Bushuev
 */
class EmptyCallback<T> : Callback<T> {

    override fun onSuccess(result: T) {
    }

    override fun onError(error: LocalException) {
    }

}
