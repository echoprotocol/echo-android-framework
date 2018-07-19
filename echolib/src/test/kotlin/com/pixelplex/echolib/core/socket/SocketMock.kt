package com.pixelplex.echolib.core.socket

import com.pixelplex.echolib.exception.LocalException

/**
 * Mock for [SocketMessenger]
 *
 * <p>
 *     Translates all events to listeners without real connection
 * </p>
 *
 * @author Dmitriy Bushuev
 */
class SocketMock : SocketMessenger {

    private var url: String? = null
    private val listeners = mutableListOf<SocketMessengerListener>()

    override fun setUrl(url: String) {
        this.url = url
    }

    override fun connect() {
        listeners.forEach { listener -> listener.onConnected() }
    }

    override fun disconnect() {
        listeners.forEach { listener -> listener.onDisconnected() }
    }

    override fun emit(message: String) {
        listeners.forEach { listener ->
            if (message == "error") listener.onFailure(LocalException()) else listener.onEvent(
                message
            )
        }
    }

    override fun on(listener: SocketMessengerListener) {
        listeners += listener
    }

    override fun off(listener: SocketMessengerListener) {
        listeners -= listener
    }

    override fun offAll() {
        listeners.clear()
    }

}
