package com.pixelplex.echolib.facade

import com.pixelplex.echolib.AccountListener
import com.pixelplex.echolib.Callback

/**
 * Encapsulates logic, associated with subscription on blockchain objects
 *
 * @author Dmitriy Bushuev
 */
interface SubscriptionFacade {

    /**
     * Subscribe [listener] on account changing events
     *
     * @param nameOrId Required account name or id
     * @param listener Listener of events, associated with required account
     */
    fun subscribeOnAccount(nameOrId: String, listener: AccountListener)

    /**
     * Unsubscribe listeners from observing account event changes
     *
     * @param nameOrId Required account name or id to unsubscribe from
     * @param callback Listener of unsubscribing process state
     *                 Receives true if unsubscribing succeed, otherwise false\error (if occurred)
     */
    fun unsubscribeFromAccount(nameOrId: String, callback: Callback<Boolean>)

    /**
     * Unsubscribe all listeners from observing all account changing events
     *
     * @param callback Listener of unsubscribing process state
     *                 Receives true if unsubscribing succeed, otherwise false\error (if occurred)
     */
    fun unsubscribeAll(callback: Callback<Boolean>)

}
