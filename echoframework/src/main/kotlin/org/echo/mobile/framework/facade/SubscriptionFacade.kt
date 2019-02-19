package org.echo.mobile.framework.facade

import org.echo.mobile.framework.AccountListener
import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.model.Block
import org.echo.mobile.framework.model.DynamicGlobalProperties
import org.echo.mobile.framework.model.Log
import org.echo.mobile.framework.service.UpdateListener

/**
 * Encapsulates logic, associated with subscription on blockchain objects
 *
 * @author Dmitriy Bushuev
 */
interface SubscriptionFacade {

    companion object {
        /**
         * Default block number for define subscriptions
         */
        const val DEFAULT_BLOCK_NUMBER = "0"
    }

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
     * Subscribe [listener] on contract logs
     * [callback] is needed for subscription process result events notifying
     *
     * @param contractId Contract id for subscribe to logs
     */
    fun subscribeOnContractLogs(
        contractId: String,
        listener: UpdateListener<List<Log>>,
        callback: Callback<Boolean>
    )

    /**
     * Unsubscribe listeners from observing block adding event
     *
     * @param callback Listener of unsubscribing process state
     *                 Receives true if unsubscribing succeed, otherwise false\error (if occurred)
     */
    fun unsubscribeFromBlock(callback: Callback<Boolean>)

    /**
     * Unsubscribe listeners from observing dynamic global properties chnages event
     *
     * @param callback Listener of unsubscribing process state
     *                 Receives true if unsubscribing succeed, otherwise false\error (if occurred)
     */
    fun unsubscribeFromBlockchainData(callback: Callback<Boolean>)

    /**
     * Unsubscribe listeners from observing contract [contractId] changes
     *
     * @param callback Listener of unsubscribing process state
     *                 Receives true if unsubscribing succeed, otherwise false\error (if occurred)
     */
    fun unsubscribeFromContractLogs(contractId: String, callback: Callback<Boolean>)

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
