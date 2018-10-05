package com.pixelplex.echoframework.facade

import com.pixelplex.echoframework.AccountListener
import com.pixelplex.echoframework.Callback
import com.pixelplex.echoframework.model.Block
import com.pixelplex.echoframework.model.DynamicGlobalProperties
import com.pixelplex.echoframework.service.UpdateListener

/**
 * Encapsulates logic, associated with subscription on blockchain objects
 *
 * @author Dmitriy Bushuev
 */
interface SubscriptionFacade {

    /**
     * Subscribe [listener] on account changing events
     *
     * @param nameOrId Required account id
     * @param listener Listener of events, associated with required account
     * @param callback Listener of subscribing process state
     *                 Receives true if subscribing succeed, otherwise false\error (if occurred)
     */
    fun subscribeOnAccount(nameOrId: String, listener: AccountListener, callback: Callback<Boolean>)

    /**
     * Subscribe [listener] on block adding events
     *
     * @param listener Listener of events, associated with new added block
     * @param callback Listener of subscribing process state
     *                 Receives true if subscribing succeed, otherwise false\error (if occurred)
     */
    fun subscribeOnBlock(listener: UpdateListener<Block>, callback: Callback<Boolean>)

    /**
     * Subscribe [listener] on dynamic global properties changes events
     *
     * @param listener Listener of events, associated with dynamic global properties changes
     * @param callback Listener of subscribing process state
     *                 Receives true if subscribing succeed, otherwise false\error (if occurred)
     */
    fun subscribeOnBlockchainData(
        listener: UpdateListener<DynamicGlobalProperties>,
        callback: Callback<Boolean>
    )

    /**
     * Unsubscribe listeners from observing account event changes with id = [nameOrId]
     *
     * @param nameOrId Required account id to unsubscribe from
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
