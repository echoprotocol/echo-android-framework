package com.pixelplex.echolib.core.socket.internal

import com.pixelplex.echolib.core.mapper.MapperCoreComponent
import com.pixelplex.echolib.core.socket.SocketCoreComponent
import com.pixelplex.echolib.core.socket.SocketMessenger
import com.pixelplex.echolib.core.socket.SocketMessengerListener
import com.pixelplex.echolib.core.socket.SocketState
import com.pixelplex.echolib.exception.LocalException
import com.pixelplex.echolib.exception.ResponseSocketException
import com.pixelplex.echolib.model.SocketResponse
import com.pixelplex.echolib.model.socketoperations.SocketOperation
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * Implementation of [SocketCoreComponent]
 *
 * @author Daria Pechkovskaya
 */
class SocketCoreComponentImpl(
    private val socketMessenger: SocketMessenger,
    private val mapper: MapperCoreComponent
) : SocketCoreComponent {

    private var callIdx = AtomicInteger(1)

    //add map locking
    private val operationsMap: ConcurrentHashMap<Int, SocketOperation<Any>> = ConcurrentHashMap()

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
        operation.callId = callIdx.getAndIncrement()

        operationsMap.putIfAbsent(operation.callId, operation as SocketOperation<Any>)

        socketMessenger.emit(operation.toJsonString() ?: "")
    }

    override fun on(listener: SocketMessengerListener) {
        socketMessenger.on(listener)
    }

    override fun off(listener: SocketMessengerListener) {
        socketMessenger.off(listener)
    }

    private inner class SocketCoreMessengerListener : SocketMessengerListener {

        private var wasReconnect = false

        override fun onEvent(event: String) {
            val response = mapper.map(event, SocketResponse::class.java)
            val operation = operationsMap.remove(response.id)
            val error = response.error

            operation?.let { op ->
                if (error != null) {
                    val localError = ResponseSocketException(error.data.message)
                    op.callback.onError(localError)
                } else {
                    mapData(event, operation)
                }
            }
        }

        private fun mapData(event: String, operation: SocketOperation<Any>) {
            try {
                val obj = operation.fromJson(event)!!
                operation.callback.onSuccess(obj)
            } catch (ex: Exception) {
                operation.callback.onError(LocalException(ex.message, ex))
            }
        }

        override fun onFailure(error: Throwable) {
            //reconnect
            //send event to operation with min id (first)
        }

        override fun onConnected() {
            socketState = SocketState.CONNECTED

            if (wasReconnect) {
                operationsMap.forEach { _, operation ->
                    socketMessenger.emit(
                        operation.toJsonString() ?: ""
                    )
                }
            }
        }

        override fun onDisconnected() {
            socketState = SocketState.DISCONNECTED
            //if was fail - reconnect by delay
        }
    }

}
