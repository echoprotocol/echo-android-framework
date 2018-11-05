package org.echo.mobile.framework.service

import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.exception.LocalException
import org.echo.mobile.framework.model.TransactionResult

/**
 * Encapsulates logic connected with broadcast transaction with callback subscription
 *
 * @author Daria Pechkovskaya
 */
interface TransactionSubscriptionManager {

    /**
     * Registers required [callback] by [callId]
     */
    fun register(callId: String, callback: Callback<TransactionResult>)

    /**
     * Removes all callbacks
     */
    fun clear()

    /**
     * Processes notifying event
     */
    fun tryProcessEvent(event: String)

}
