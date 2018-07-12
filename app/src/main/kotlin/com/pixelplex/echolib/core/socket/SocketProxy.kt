package com.pixelplex.echolib.core.socket

/**
 * Encapsulates messages proxying logic and listeners managing
 *
 * @author Dmitriy Bushuev
 */
interface SocketProxy {

    /**
     * Emits [message] through socket connection
     *
     * @param message Message to emit
     */
    fun emit(message: String)

    /**
     * Subscribes [listener] on socket events
     *
     * @param listener Connection url
     */
    fun on(listener: SocketMessengerListener)

    /**
     * Unsubscribes [listener] from socket events
     *
     * @param listener Connection url
     */
    fun off(listener: SocketMessengerListener)

}
