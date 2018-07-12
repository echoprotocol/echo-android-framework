package com.pixelplex.echolib.core.socket.internal

import com.pixelplex.echolib.core.socket.SocketMessengerListener
import com.pixelplex.echolib.core.socket.SocketMessenger

/**
 * Default implementation of [SocketMessenger]
 *
 * @author Dmitriy Bushuev
 */
class SocketMessengerImpl : SocketMessenger {

    override fun setUrl(url: String) {
    }

    override fun connect() {
    }

    override fun disconnect() {
    }

    override fun emit(message: String) {
    }

    override fun on(listener: SocketMessengerListener) {
    }

    override fun off(listener: SocketMessengerListener) {
    }

}
