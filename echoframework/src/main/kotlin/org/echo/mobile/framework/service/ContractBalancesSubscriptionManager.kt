package org.echo.mobile.framework.service

import org.echo.mobile.framework.model.contract.ContractBalance

/**
 * Encapsulates logic connected with contract subscription listeners notifying
 *
 * @author Daria Pechkovskaya
 */
interface ContractBalancesSubscriptionManager {

    /**
     * Registers listener
     */
    fun addListener(listener: UpdateListener<Map<String, List<ContractBalance>>>)

    /**
     * Checks if manager contains any listeners
     */
    fun containsListeners(): Boolean

    /**
     * Checks, whether listener already registered
     */
    fun registered(listener: UpdateListener<Map<String, List<ContractBalance>>>): Boolean

    /**
     * Removes listener
     */
    fun removeListener(listener: UpdateListener<Map<String, List<ContractBalance>>>): Boolean

    /**
     * Removes all listeners
     */
    fun clear()

    /**
     * Notifies contract [data] for listeners
     */
    fun notify(data: Map<String, List<ContractBalance>>)

    /**
     * Processes notifying event
     */
    fun processEvent(event: String): Map<String, List<ContractBalance>>?

}
