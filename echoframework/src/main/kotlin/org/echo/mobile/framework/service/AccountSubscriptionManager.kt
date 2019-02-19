package org.echo.mobile.framework.service

import org.echo.mobile.framework.AccountListener
import org.echo.mobile.framework.model.FullAccount

/**
 * Encapsulates logic connected with account subscription event parsing and listeners notifying
 *
 * @author Dmitriy Bushuev
 */
interface AccountSubscriptionManager {

    /**
     * Registers required listener by account id
     */
    fun registerListener(id: String, listener: AccountListener)

    /**
     * Checks if manager contains any listeners
     */
    fun containsListeners(): Boolean

    /**
     * Check, whether listeners already registered by this id
     */
    fun registered(id: String): Boolean

    /**
     * Removes listener by id
     */
    fun removeListeners(id: String): MutableList<AccountListener>?

    /**
     * Removes all listeners
     */
    fun clear()

    /**
     * Notifies listeners, connected with input account
     */
    fun notify(account: FullAccount)

    /**
     * Processes notifying event. Returns list of parsed ids
     */
    fun processEvent(event: String): List<String>

}
