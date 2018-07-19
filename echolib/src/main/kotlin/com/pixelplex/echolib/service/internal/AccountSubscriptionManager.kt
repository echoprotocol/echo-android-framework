package com.pixelplex.echolib.service.internal

import com.pixelplex.echolib.AccountListener
import com.pixelplex.echolib.model.Account

/**
 * Encapsulates logic connected with account subscription event parsing and listeners notifying
 *
 * @author Dmitriy Bushuev
 */
interface AccountSubscriptionManager {

    /**
     * Registers required listener by account id
     */
    fun registerListener(id: String, listener: AccountListener): Boolean

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
    fun notify(account: Account)

    /**
     * Processes event
     *
     * @return Account id, parsed from event
     */
    fun processEvent(event: String): String?

}
