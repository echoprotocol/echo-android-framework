package com.pixelplex.echolib.support

import com.pixelplex.echolib.Callback
import com.pixelplex.echolib.exception.LocalException

/**
 * Empty instance of operation callback
 *
 * <p>
 *     Useful when passing callback is unnecessary
 * </p>
 *
 * @author Dmitriy Bushuev
 */
class EmptyCallback<T> : Callback<T> {

    override fun onSuccess(result: T) {
    }

    override fun onError(error: LocalException) {
    }

}
