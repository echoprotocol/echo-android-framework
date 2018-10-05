package com.pixelplex.echoframework.service

import com.pixelplex.echoframework.model.Block

/**
 * Encapsulates logic connected with block subscription event parsing and listeners notifying
 *
 * @author Daria Pechkovskaya
 */
interface BlockSubscriptionManager {

    /**
     * Registers required listener
     */
    fun addListener(listener: UpdateListener<Block>)

    /**
     * Checks if manager contains any listeners
     */
    fun containListeners(): Boolean

    /**
     * Checks if manager contains [listener]
     */
    fun containsListener(listener: UpdateListener<Block>): Boolean

    /**
     * Removes listener
     */
    fun removeListener(listener: UpdateListener<Block>)

    /**
     * Removes all listeners
     */
    fun clear()

    /**
     * Notifies listeners, connected with input data item
     */
    fun notify(block: Block)
}

