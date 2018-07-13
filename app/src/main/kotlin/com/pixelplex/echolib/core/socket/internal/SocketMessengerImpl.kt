package com.pixelplex.echolib.core.socket.internal

import com.neovisionaries.ws.client.*
import com.pixelplex.echolib.core.socket.SocketMessenger
import com.pixelplex.echolib.core.socket.SocketMessengerListener

/**
 * Default implementation of [SocketMessenger]
 *
 * @author Dmitriy Bushuev
 */
class SocketMessengerImpl : SocketMessenger {

    @Volatile
    private var isOpen = false
    private var webSocket: WebSocket? = null

    private var url: String? = null

    private val listeners: MutableList<SocketMessengerListener> = mutableListOf()

    override fun setUrl(url: String) {
        this.url = url
    }

    override fun connect() {
        if (!isOpen && webSocket == null) {
            synchronized(this) {
                if (!isOpen && webSocket == null) {
                    init()
                }
            }
        }
    }

    private fun init() {
        val webSocket = WebSocketFactory().let { factory ->
            factory.connectionTimeout = CONNECTION_TIMEOUT
            factory.createSocket(url)
        }

        webSocket.addListener(SocketEventsCallback())
        this.webSocket = webSocket
        webSocket.connectAsynchronously()
    }

    override fun disconnect() {
        if (isOpen) {
            webSocket?.disconnect()
        }
    }

    override fun emit(message: String) {
        if (isOpen) {
            webSocket?.sendText(message)
        }
    }

    override fun on(listener: SocketMessengerListener) {
        listeners += listener
    }

    override fun off(listener: SocketMessengerListener) {
        listeners.remove(listener)
    }

    override fun offAll() {
        listeners.clear()
    }

    private inner class SocketEventsCallback : WebSocketAdapter() {

        override fun onConnected(
            websocket: WebSocket?,
            headers: MutableMap<String, MutableList<String>>?
        ) {
            isOpen = true

            listeners.forEach { listener -> listener.onConnected() }
        }

        override fun onTextFrame(websocket: WebSocket?, frame: WebSocketFrame) {
            listeners.forEach { listener -> listener.onEvent(frame.payloadText) }
        }

        override fun onError(websocket: WebSocket?, cause: WebSocketException) {
            webSocket = null
            isOpen = false

            listeners.forEach { listener -> listener.onFailure(cause) }
        }

        override fun onDisconnected(
            websocket: WebSocket?,
            serverCloseFrame: WebSocketFrame?,
            clientCloseFrame: WebSocketFrame?,
            closedByServer: Boolean
        ) {
            webSocket = null
            isOpen = false


            listeners.forEach { listener -> listener.onDisconnected() }
        }

    }

    companion object {
        private const val CONNECTION_TIMEOUT = 10000
    }

}
