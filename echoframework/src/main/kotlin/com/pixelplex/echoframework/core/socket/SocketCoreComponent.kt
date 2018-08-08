package com.pixelplex.echoframework.core.socket

import com.pixelplex.echoframework.model.socketoperations.SocketOperation

/**
 * Encapsulates logic, associated with socket API calls to Graphene blockchain
 *
 * @author Dmitriy Bushuev
 */
interface SocketCoreComponent {

    val socketState: SocketState

    val currentId: Int

    /**
     * Connects to socket by url
     * @param url Url for socket connection
     */
    fun connect(url: String)

    /**
     * Disconnects from socket, removes all listeners
     */
    fun disconnect()

    /**
     * Send operation to blockchain
     * @param operation Operation to emit to blockchain
     */
    fun emit(operation: SocketOperation<*>)

    /**
     * Adds listener to socket messenger
     */
    fun on(listener: SocketMessengerListener)

    /**
     * Removes listener from socket messenger
     */
    fun off(listener: SocketMessengerListener)
}
