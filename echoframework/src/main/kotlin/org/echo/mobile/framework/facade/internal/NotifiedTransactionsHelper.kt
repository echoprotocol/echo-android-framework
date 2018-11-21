package org.echo.mobile.framework.facade.internal

import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.core.socket.SocketCoreComponent
import org.echo.mobile.framework.core.socket.SocketMessengerListener
import org.echo.mobile.framework.model.TransactionResult
import org.echo.mobile.framework.model.network.Network
import org.echo.mobile.framework.service.TransactionSubscriptionManager
import org.echo.mobile.framework.service.internal.subscription.TransactionSubscriptionManagerImpl
import org.echo.mobile.framework.support.toJsonObject

/**
 * Includes logic for notified transactions assembly
 *
 * @author Daria Pechkovskaya
 */
class NotifiedTransactionsHelper(
    private val socketCoreComponent: SocketCoreComponent,
    private val network: Network
) {

    @Volatile
    private var subscribed = false

    private val socketMessengerListener by lazy {
        SubscriptionListener()
    }

    private val transactionSubscriptionManager: TransactionSubscriptionManager by lazy {
        TransactionSubscriptionManagerImpl(network)
    }

    /**
     * Subscribes on notifying transaction result by [callId]
     */
    fun subscribeOnTransactionResult(callId: String, callback: Callback<TransactionResult>) {
        subscribeSocket()
        transactionSubscriptionManager.register(callId, callback)
    }

    private fun subscribeSocket() {
        if (!subscribed) {
            subscribed = true
            socketCoreComponent.on(socketMessengerListener)
        }
    }

    private inner class SubscriptionListener : SocketMessengerListener {

        override fun onEvent(event: String) {
            // no need to process other events
            if (event.toJsonObject()?.get(METHOD_KEY)?.asString != NOTICE_METHOD_KEY) {
                return
            }

            transactionSubscriptionManager.tryProcessEvent(event)
        }

        override fun onFailure(error: Throwable) = resetState()

        override fun onConnected() {}

        override fun onDisconnected() = resetState()
    }

    private fun resetState() {
        subscribed = false
        socketCoreComponent.off(socketMessengerListener)
        transactionSubscriptionManager.clear()
    }


    companion object {
        const val METHOD_KEY = "method"
        private const val NOTICE_METHOD_KEY = "notice"
    }

}
