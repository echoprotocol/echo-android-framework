package org.echo.mobile.framework.core.socket.internal

import com.neovisionaries.ws.client.*
import org.echo.mobile.framework.core.logger.internal.LoggerCoreComponent
import org.echo.mobile.framework.core.socket.SocketMessenger
import org.echo.mobile.framework.core.socket.SocketMessengerListener

/**
 * Default implementation of [SocketMessenger]
 *
 * @author Dmitriy Bushuev
 */
class SocketMessengerImpl : SocketMessenger {

    @Volatile
    private var isOpen = false
    private var webSocket: WebSocket? = null

    private lateinit var url: String

    private val listeners = mutableListOf<SocketMessengerListener>()

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
        }.apply {
            pongInterval = HEART_BEAT_INTERVAL
            addListener(SocketEventsCallback())
        }

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
            LOGGER.log(">>>> $message")
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
            LOGGER.log("Socket connected")

            isOpen = true

            listeners.forEach { listener -> listener.onConnected() }
        }

        override fun onTextFrame(websocket: WebSocket?, frame: WebSocketFrame) {
            LOGGER.log("<<<< ${frame.payloadText}")
            listeners.forEach { listener -> listener.onEvent(frame.payloadText) }
        }

        override fun onError(websocket: WebSocket?, cause: WebSocketException) {
            LOGGER.log("Socket error", cause)

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
            LOGGER.log(
                """"Socket disconnected. Closed by server = $closedByServer.
                        "Client close message = ${clientCloseFrame?.payloadText}.
                        "Server close message = ${serverCloseFrame?.payloadText}"""
            )

            webSocket = null
            isOpen = false

            listeners.forEach { listener -> listener.onDisconnected() }
        }

    }

    companion object {
        private const val CONNECTION_TIMEOUT = 10000
        private const val HEART_BEAT_INTERVAL: Long = 20000

        private val LOGGER = LoggerCoreComponent.create(SocketMessengerImpl::class.java.name)
    }

}
