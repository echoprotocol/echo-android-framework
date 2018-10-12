package org.echo.mobile.framework.service.internal.subscription

import org.echo.mobile.framework.model.contract.Contract
import org.echo.mobile.framework.service.ContractSubscriptionManager
import org.echo.mobile.framework.service.UpdateListener
import java.util.concurrent.ConcurrentHashMap

/**
 * Implementation of [ContractSubscriptionManager]
 *
 * @author Dmitriy Bushuev
 */
class ContractSubscriptionManagerImpl : ContractSubscriptionManager {

    private val listeners = ConcurrentHashMap<String, MutableList<UpdateListener<Contract>>>()

    override fun registerListener(id: String, listener: UpdateListener<Contract>) {
        val accountListeners = listeners[id]

        if (accountListeners == null) {
            val listenersByName = mutableListOf(listener)
            listeners[id] = listenersByName
        } else {
            accountListeners += listener
        }
    }

    override fun containsListeners(): Boolean = listeners.isNotEmpty()

    override fun registered(id: String): Boolean = listeners.containsKey(id)

    override fun removeListeners(id: String): MutableList<UpdateListener<Contract>>? =
        listeners.remove(id)

    override fun clear() = listeners.clear()

    override fun notify(contract: Contract) {
        val objectId = contract.getObjectId()
        listeners[objectId]?.forEach { listener ->
            listener.onUpdate(contract)
        }
    }

}
