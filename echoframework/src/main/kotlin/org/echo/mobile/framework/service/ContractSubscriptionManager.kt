package org.echo.mobile.framework.service

import org.echo.mobile.framework.model.contract.Contract

/**
 * Encapsulates logic connected with contract subscription listeners notifying
 *
 * @author Dmitriy Bushuev
 */
interface ContractSubscriptionManager {

    /**
     * Registers required listener by contract [id]
     */
    fun registerListener(id: String, listener: UpdateListener<Contract>)

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
    fun removeListeners(id: String): MutableList<UpdateListener<Contract>>?

    /**
     * Removes all listeners
     */
    fun clear()

    /**
     * Notifies listeners, connected with input [contract]
     */
    fun notify(contract: Contract)

}
