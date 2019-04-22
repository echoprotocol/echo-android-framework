package org.echo.mobile.framework.service

import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.model.RegistrationResult

/**
 * Encapsulates logic connected with blockchain notifications subscription
 *
 * @author Daria Pechkovskaya
 */
interface NotifiedSubscriptionManager<T> {

    /**
     * Registers required [callback] by [callId]
     */
    fun register(callId: String, callback: Callback<T>)

    /**
     * Removes all callbacks
     */
    fun clear()

    /**
     * Processes notifying event
     */
    fun tryProcessEvent(event: String)

}
