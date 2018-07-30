package com.pixelplex.echoframework.core.socket

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.pixelplex.echoframework.Callback
import com.pixelplex.echoframework.core.mapper.MapperCoreComponent
import com.pixelplex.echoframework.core.socket.internal.SocketCoreComponentImpl
import com.pixelplex.echoframework.exception.LocalException
import com.pixelplex.echoframework.model.socketoperations.SocketMethodType
import com.pixelplex.echoframework.model.socketoperations.SocketOperation
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Test cases for [SocketCoreComponentImpl]
 *
 * @author Dmitriy Bushuev
 */
class SocketCoreComponentTest {

    private lateinit var socketMock: SocketMessenger
    private lateinit var mapperMock: MapperCoreComponent
    private lateinit var socketCore: SocketCoreComponent

    @Before
    fun setUp() {
        socketMock = SocketMock()
        mapperMock = MapperMock()
    }

    @Test
    fun connectTest() {
        socketCore = SocketCoreComponentImpl(socketMock, mapperMock)

        var connected = false
        socketCore.on(object : SocketMessengerListener {
            override fun onFailure(error: Throwable) {
            }

            override fun onConnected() {
                connected = true
            }

            override fun onDisconnected() {
            }

            override fun onEvent(event: String) {
            }
        })

        socketCore.connect("testurl")

        assertTrue(connected)
    }

    @Test
    fun disconnectTest() {
//        socketCore = SocketCoreComponentImpl(socketMock, mapperMock)
//
//        var disconnected = false
//        socketCore.on(object : SocketMessengerListener {
//            override fun onFailure(error: Throwable) {
//            }
//
//            override fun onConnected() {
//            }
//
//            override fun onDisconnected() {
//                disconnected = true
//            }
//
//            override fun onEvent(event: String) {
//            }
//        })
//
//        socketCore.connect("testurl")
//        socketCore.disconnect()
//
//        Assert.assertTrue(disconnected)
    }

    @Test
    fun simpleNotifyOperationTest() {
        socketCore = SocketCoreComponentImpl(socketMock, mapperMock)
        socketCore.connect("testurl")

        var succeed = false
        socketCore.emit(
            OperationMock(
                1,
                object : Callback<String> {
                    override fun onError(error: LocalException) {
                    }

                    override fun onSuccess(result: String) {
                        succeed = true
                    }

                })
        )

        assertTrue(succeed)
    }

    @Test
    fun operationRemoveAfterNotifyTest() {
        socketCore = SocketCoreComponentImpl(socketMock, mapperMock)
        socketCore.connect("testurl")

        var notifyCount = 0
        socketCore.emit(
            OperationMock(
                1,
                object : Callback<String> {
                    override fun onError(error: LocalException) {
                    }

                    override fun onSuccess(result: String) {
                        notifyCount++
                    }

                })
        )

        socketMock.emit("1")

        assertTrue(notifyCount == 1)
    }

    @Test(expected = Exception::class)
    fun parseExceptionTest() {
        socketCore = SocketCoreComponentImpl(socketMock, mapperMock)
        socketCore.connect("testurl")

        // need more specific exception
        socketMock.emit("notInt")
    }

    private class OperationMock(override val apiId: Int, callback: Callback<String>) :
        SocketOperation<String>(SocketMethodType.CALL, 1, String::class.java, callback) {

        override fun createParameters(): JsonElement = JsonObject()

        override fun fromJson(json: String): String? = json

        override fun toJsonString(): String? {
            return callId.toString()
        }
    }

    private class OperationErrorMock(override val apiId: Int, callback: Callback<String>) :
        SocketOperation<String>(SocketMethodType.CALL, 1, String::class.java, callback) {

        override fun createParameters(): JsonElement = JsonObject()

        override fun fromJson(json: String): String? = json

        override fun toJsonString(): String? {
            return "error"
        }
    }

}
