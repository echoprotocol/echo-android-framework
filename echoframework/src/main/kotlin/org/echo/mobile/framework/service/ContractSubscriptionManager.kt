package org.echo.mobile.framework.service

import org.echo.mobile.framework.model.Log

/**
 * Encapsulates logic connected with contract subscription listeners notifying
 *
 * @author Dmitriy Bushuev
 */
interface ContractSubscriptionManager {

    /**
     * Registers required listener by contract [id]
     */
    fun registerListener(id: String, listener: UpdateListener<List<Log>>)

    /**
     * Checks if manager contains any listeners
     */
    fun containsListeners(): Boolean

    /**
     * Checks, whether listeners already registered by this [id]
     */
    fun registered(id: String): Boolean

    /**
     * Removes listener by [id]
     */
    fun removeListeners(id: String): MutableList<UpdateListener<List<Log>>>?

    /**
     * Removes all listeners
     */
    fun clear()

    /**
     * Notifies contract [logs] for listeners, connected with input [contractId]
     */
    fun notify(contractId: String, logs: List<Log>)

    /**
     * Processes notifying event.
     */
    fun processEvent(event: String): Map<String, List<Log>>

}
