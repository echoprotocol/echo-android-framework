package org.echo.mobile.framework.model.socketoperations

import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.model.BaseResult

/**
 * Encapsulates operation state
 *
 * Provides abstraction over operation confirmation results
 *
 * @author Pavel Savchkov
 */
data class ResultCallback<T : BaseResult>(private val callback: Callback<T>) {

    /**
     * Returns [callback] function
     */
    fun get(): Callback<T> {
        return callback
    }
}