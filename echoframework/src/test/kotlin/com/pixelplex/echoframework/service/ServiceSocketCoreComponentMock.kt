package com.pixelplex.echoframework.service

import com.pixelplex.echoframework.core.socket.SocketCoreComponent
import com.pixelplex.echoframework.core.socket.SocketMessengerListener
import com.pixelplex.echoframework.core.socket.SocketState
import com.pixelplex.echoframework.exception.LocalException
import com.pixelplex.echoframework.model.socketoperations.SocketOperation

/**
 * [SocketCoreComponent] mock.
 *
 * Return defined [response] on every [emit] call.
 * When [response] is not defined (==null) then error returns
 *
 * @author Dmitriy Bushuev
 */
class ServiceSocketCoreComponentMock<T>(private val response: T) : SocketCoreComponent {

    override val socketState: SocketState = SocketState.CONNECTED

    override val currentId: Int = 1

    override fun connect(url: String) {
    }

    override fun disconnect() {
    }

    override fun emit(operation: SocketOperation<*>) {
        response?.let { (operation as SocketOperation<T>).callback.onSuccess(response) }
            ?: operation.callback.onError(
                LocalException("Test error. Empty response")
            )
    }

    override fun on(listener: SocketMessengerListener) {
    }

    override fun off(listener: SocketMessengerListener) {
    }

}
