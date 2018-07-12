package com.pixelplex.echolib.core.socket.internal

import com.pixelplex.echolib.core.socket.SocketCoreComponent
import com.pixelplex.echolib.core.socket.SocketMessenger
import com.pixelplex.echolib.core.socket.SocketMessengerListener
import com.pixelplex.echolib.core.socket.SocketState
import com.pixelplex.echolib.model.socketoperations.SocketOperation
import com.pixelplex.echolib.support.model.Api

/**
 * Implementation of [SocketCoreComponent]
 *
 * @author Daria Pechkovskaya
 */
class SocketCoreComponentImpl(
    private val socketMessenger: SocketMessenger,
    private val apis: Set<Api>
) : SocketCoreComponent {

    @Volatile
    private var callIndex = 0
    private val operationsMap = hashMapOf<Int, SocketOperation>()

    override var socketState: SocketState = SocketState.DISCONNECTED

    init {
        socketMessenger.on(object : SocketMessengerListener {
            override fun onEvent(event: String) {
                //parse event
            }

            override fun onFailure(error: Throwable) {

            }

            override fun onConnected() {
                socketState = SocketState.CONNECTED

            }

            override fun onDisconnected() {
                socketState = SocketState.DISCONNECTED
            }
        })
    }

    override fun send(operation: SocketOperation) {
        operationsMap[callIndex] = operation
        socketMessenger.emit(operation.toJsonString() ?: "")
    }

}
