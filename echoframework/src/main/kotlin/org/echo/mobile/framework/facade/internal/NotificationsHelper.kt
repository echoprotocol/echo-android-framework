package org.echo.mobile.framework.facade.internal

import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.core.socket.SocketCoreComponent
import org.echo.mobile.framework.core.socket.SocketMessengerListener
import org.echo.mobile.framework.service.NotifiedSubscriptionManager
import org.echo.mobile.framework.support.toJsonObject

/**
 * Includes logic for notified data assembly
 *
 * @author Daria Pechkovskaya
 */
class NotificationsHelper<T>(
    private val socketCoreComponent: SocketCoreComponent,
    private val manager: NotifiedSubscriptionManager<T>
) {

    @Volatile
    private var subscribed = false

    private val socketMessengerListener by lazy {
        SubscriptionListener()
    }

    /**
     * Subscribes on notifying transaction result by [callId]
     */
    fun subscribeOnResult(callId: String, callback: Callback<T>) {
        subscribeSocket()
        manager.register(callId, callback)
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

            manager.tryProcessEvent(event)
        }

        override fun onFailure(error: Throwable) = resetState()

        override fun onConnected() {}

        override fun onDisconnected() = resetState()
    }

    private fun resetState() {
        subscribed = false
        socketCoreComponent.off(socketMessengerListener)
        manager.clear()
    }


    companion object {
        const val METHOD_KEY = "method"
        private const val NOTICE_METHOD_KEY = "notice"
    }

}
