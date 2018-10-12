package org.echo.mobile.framework.service.internal.subscription

import org.echo.mobile.framework.model.Block
import org.echo.mobile.framework.service.BlockSubscriptionManager
import org.echo.mobile.framework.service.UpdateListener

/**
 * Implementation of [BlockSubscriptionManager]
 *
 * @author Daria Pechkovskaya
 */
class BlockSubscriptionManagerImpl : BlockSubscriptionManager {

    private val listeners = mutableListOf<UpdateListener<Block>>()

    override fun addListener(listener: UpdateListener<Block>) {
        listeners.add(listener)
    }

    override fun containListeners(): Boolean = listeners.isNotEmpty()

    override fun containsListener(listener: UpdateListener<Block>): Boolean = listeners.contains(listener)

    override fun removeListener(listener: UpdateListener<Block>) {
        listeners.remove(listener)
    }

    override fun clear() = listeners.clear()

    override fun notify(block: Block) {
        listeners.forEach { listener ->
            listener.onUpdate(block)
        }
    }
}
