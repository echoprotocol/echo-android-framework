package org.echo.mobile.framework.service

import org.echo.mobile.framework.core.mapper.ObjectMapper
import org.echo.mobile.framework.model.DynamicGlobalProperties

/**
 * Encapsulates logic connected with current blockchain data subscription event parsing
 * and listeners notifying
 *
 * @author Daria Pechkovskaya
 */
interface CurrentBlockchainDataSubscriptionManager {

    companion object {
        /**
         * Object id for retrieve current blockchain data
         */
        const val CURRENT_BLOCKCHAIN_DATA_OBJECT_ID = "2.1.0"
    }

    /**
     * [ObjectMapper] for map [DynamicGlobalProperties] data
     */
    val mapper: ObjectMapper<DynamicGlobalProperties>

    /**
     * Adds required listener
     */
    fun addListener(listener: UpdateListener<DynamicGlobalProperties>)

    /**
     * Checks if manager contains any listeners
     */
    fun containListeners(): Boolean

    /**
     * Checks if manager contains [listener]
     */
    fun containsListener(listener: UpdateListener<DynamicGlobalProperties>): Boolean

    /**
     * Removes listener
     */
    fun removeListener(listener: UpdateListener<DynamicGlobalProperties>)

    /**
     * Removes all listeners
     */
    fun clear()

    /**
     * Notifies listeners, connected with input data item
     */
    fun notify(blockchainData: DynamicGlobalProperties)

    /**
     * Processes notifying event.
     */
    fun processEvent(event: String): DynamicGlobalProperties?
}

