package com.pixelplex.echolib.core.socket

/**
 * Encapsulates socket connection establishment logic
 *
 * @author Dmitriy Bushuev
 */
interface SocketConnection {

    /**
     * Defines base url for socket connection
     *
     * @param url Connection url
     */
    fun setUrl(url: String)

    /**
     * Starts socket connectivity process with specified url
     */
    fun connect()

    /**
     * Breaks sockets connection
     */
    fun disconnect()

}

/**
 * State of socket connection
 *
 * @author Daria Pechkovskaya
 */
enum class SocketState {
    CONNECTED,
    DISCONNECTED
}
