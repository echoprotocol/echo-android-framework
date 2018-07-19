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
     * @param id Required account id
     * @param listener Listener of events, associated with required account
     */
    fun subscribeOnAccount(id: String, listener: AccountListener)

    /**
     * Unsubscribe listeners from observing account event changes with id = [id]
     *
     * @param id       Required account id to unsubscribe from
     * @param callback Listener of unsubscribing process state
     *                 Receives true if unsubscribing succeed, otherwise false\error (if occurred)
     */
    fun unsubscribeFromAccount(id: String, callback: Callback<Boolean>)

    /**
     * Unsubscribe all listeners from observing all account changing events
     *
     * @param callback Listener of unsubscribing process state
     *                 Receives true if unsubscribing succeed, otherwise false\error (if occurred)
     */
    fun unsubscribeAll(callback: Callback<Boolean>)

}
