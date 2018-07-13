package com.pixelplex.echolib.core.socket.internal

import com.pixelplex.echolib.core.MapperCoreComponent
import com.pixelplex.echolib.core.socket.SocketCoreComponent
import com.pixelplex.echolib.core.socket.SocketMessenger
import com.pixelplex.echolib.core.socket.SocketMessengerListener
import com.pixelplex.echolib.core.socket.SocketState
import com.pixelplex.echolib.exception.ResponseSocketException
import com.pixelplex.echolib.model.SocketResponse
import com.pixelplex.echolib.model.socketoperations.SocketOperation
import kotlin.properties.Delegates

/**
 * Implementation of [SocketCoreComponent]
 *
 * @author Daria Pechkovskaya
 */
class SocketCoreComponentImpl(
    private val socketMessenger: SocketMessenger,
    private val mapper: MapperCoreComponent
) : SocketCoreComponent {

    @Volatile
    private var callIndex = 1

    //add map locking
    private val operationsMap: HashMap<Int, SocketOperation<Any>>
            by Delegates.observable(hashMapOf()) { _, oldValue, newValue ->
                if (oldValue.size < newValue.size) {
                    ++callIndex
                }
            }

    private val globalSocketListener = SocketCoreMessengerListener()

    override var socketState: SocketState = SocketState.DISCONNECTED

    override fun connect(url: String) {
        socketMessenger.on(globalSocketListener)
        socketMessenger.setUrl(url)
        socketMessenger.connect()
    }

    override fun disconnect() {
        socketMessenger.offAll()
    }

    @Suppress("unchecked_cast")
    override fun emit(operation: SocketOperation<*>) {
        operation.callId = callIndex

        operationsMap[callIndex] = operation as SocketOperation<Any>

        socketMessenger.emit(operation.toJsonString() ?: "")
    }

    override fun on(listener: SocketMessengerListener) {
        socketMessenger.on(listener)
    }

    override fun off(listener: SocketMessengerListener) {
        socketMessenger.off(listener)
    }

    private inner class SocketCoreMessengerListener : SocketMessengerListener {

        override fun onEvent(event: String) {
            val response = mapper.map(event, SocketResponse::class.java)
            val operation = operationsMap[response.id]
            val error = response.error

            operation?.let { op ->
                operationsMap.remove(response.id)

                if (error != null) {
                    val localError = ResponseSocketException(error.data.message)
                    op.callback.onError(localError)

                } else {
                    val mapObj = mapper.map(event, op.type)
                    op.callback.onSuccess(mapObj)
                }
            }
        }

        override fun onFailure(error: Throwable) {
            //reconnect
            //send event to operation with min id (first)
        }

        override fun onConnected() {
            socketState = SocketState.CONNECTED

            operationsMap.forEach { _, operation -> emit(operation) }
        }

        override fun onDisconnected() {
            socketState = SocketState.DISCONNECTED
            //if was fail - reconnect by delay
        }
    }

}
