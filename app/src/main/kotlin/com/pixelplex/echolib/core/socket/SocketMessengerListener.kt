package com.pixelplex.echolib.core.socket

/**
 * Listener for [SocketMessenger] events
 *
 * @author Dmitriy Bushuev
 */
interface SocketMessengerListener {

    /**
     * Signals an incoming event
     *
     * @param event String Event in string representation
     */
    fun onEvent(event: String)

    /**
     * Signals an error occurred during data exchanging process
     *
     * @param error Error description
     */
    fun onFailure(error: Throwable)

    /**
     * Signals socket connection succeed
     */
    fun onConnect()

}
