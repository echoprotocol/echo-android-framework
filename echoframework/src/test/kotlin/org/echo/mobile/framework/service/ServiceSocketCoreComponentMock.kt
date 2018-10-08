package org.echo.mobile.framework.service

import org.echo.mobile.framework.core.socket.SocketCoreComponent
import org.echo.mobile.framework.core.socket.SocketMessengerListener
import org.echo.mobile.framework.core.socket.SocketState
import org.echo.mobile.framework.exception.LocalException
import org.echo.mobile.framework.model.socketoperations.SocketOperation

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
