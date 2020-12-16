package org.echo.mobile.framework.model.socketoperations

import org.echo.mobile.framework.support.Result
import java.lang.Exception

/**
 * Encapsulates transaction state
 *
 * Provides abstraction over transaction confirmation results
 *
 * @author Pavel Savchkov
 */
data class TransactionResultCallback(private val callback: (Result<Exception, Boolean>) -> Unit) {

    /**
     * Returns [callback] function
     */
    fun get(): (Result<Exception, Boolean>) -> Unit {
        return callback
    }

    /**
     * Invokes callback function with given param
     *
     * @param value is the transaction result
     */
    fun processResult(value: Result<Exception, Boolean>) {
        callback.invoke(value)
    }
}