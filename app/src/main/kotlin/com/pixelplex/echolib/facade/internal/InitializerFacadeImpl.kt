package com.pixelplex.echolib.facade.internal

import com.pixelplex.echolib.Callback
import com.pixelplex.echolib.core.socket.SocketCoreComponent
import com.pixelplex.echolib.core.socket.SocketMessengerListener
import com.pixelplex.echolib.exception.LocalException
import com.pixelplex.echolib.exception.SocketException
import com.pixelplex.echolib.facade.InitializerFacade
import com.pixelplex.echolib.model.socketoperations.AccessSocketOperation
import com.pixelplex.echolib.model.socketoperations.AccessSocketOperationType
import com.pixelplex.echolib.model.socketoperations.SocketOperation
import com.pixelplex.echolib.support.Api

/**
 * Implementation of [InitializerFacade]
 *
 * @author Daria Pechkovskaya
 */
class InitializerFacadeImpl(
    private val socketCoreComponent: SocketCoreComponent,
    private val url: String,
    private val apis: Set<Api>
) : InitializerFacade {

    private val initializeSocketListener by lazy { InitializeSocketListener() }

    private var connectingCallback: Callback<Any>? = null

    private var apisCount: Int = 0
        get() = apis.size

    override fun connect(callback: Callback<Any>) {
        this.connectingCallback = callback

        socketCoreComponent.on(initializeSocketListener)
        socketCoreComponent.connect(url)
    }

    private fun connectBlockchainApis(apis: Set<Api>) {
        if (apis.isEmpty()) {
            handleCallbackError(SocketException("Cannot find blockchain api for connection")) //?
            return
        }

        login(object : Callback<Int> {
            override fun onSuccess(result: Int) {
                val apisOperations = createApiOperations(apis)
                apisOperations.forEach { operation -> socketCoreComponent.emit(operation) }
            }

            override fun onError(error: LocalException) {
                handleCallbackError(error)
            }
        })
    }

    private fun login(callback: Callback<Int>) {
        val loginOperation = AccessSocketOperation(
            accessSocketType = AccessSocketOperationType.LOGIN,
            api = InitializerFacade.INITIALIZER_API_ID,
            callback = callback
        )

        socketCoreComponent.emit(loginOperation)
    }

    private fun createApiOperations(apis: Set<Api>): List<SocketOperation<*>> {
        val operations = arrayListOf<SocketOperation<*>>()

        apis.forEach { api ->
            when (api) {
                Api.DATABASE -> AccessSocketOperation(
                    accessSocketType = AccessSocketOperationType.DATABASE,
                    api = InitializerFacade.INITIALIZER_API_ID,
                    callback = apisCallback
                )
                Api.NETWORK_BROADCAST -> AccessSocketOperation(
                    accessSocketType = AccessSocketOperationType.NETWORK_BROADCAST,
                    api = InitializerFacade.INITIALIZER_API_ID,
                    callback = apisCallback
                )
                Api.ACCOUNT_HISTORY -> AccessSocketOperation(
                    accessSocketType = AccessSocketOperationType.HISTORY,
                    api = InitializerFacade.INITIALIZER_API_ID,
                    callback = apisCallback
                )
                Api.CRYPTO -> AccessSocketOperation(
                    accessSocketType = AccessSocketOperationType.CRYPTO,
                    api = InitializerFacade.INITIALIZER_API_ID,
                    callback = apisCallback
                )
                Api.NETWORK_NODES -> AccessSocketOperation(
                    accessSocketType = AccessSocketOperationType.NETWORK_NODES,
                    api = InitializerFacade.INITIALIZER_API_ID,
                    callback = apisCallback
                )
            }
        }

        return operations
    }

    private val apisCallback: Callback<Int> by lazy {
        object : Callback<Int> {

            override fun onSuccess(result: Int) {
                updateCallback()
            }

            override fun onError(error: LocalException) {
                handleCallbackError(error)
            }
        }
    }

    private fun updateCallback() {
        --apisCount
        if (apisCount == 0) {
            connectingCallback?.onSuccess(Any())
            connectingCallback = null
            socketCoreComponent.off(initializeSocketListener)
        }
    }

    private fun handleCallbackError(error: LocalException) {
        connectingCallback?.onError(error)
        connectingCallback = null
        socketCoreComponent.off(initializeSocketListener)
    }

    private inner class InitializeSocketListener : SocketMessengerListener {

        override fun onEvent(event: String) {}

        override fun onFailure(error: Throwable) {
            val localError = LocalException(error.message)
            handleCallbackError(localError)
        }

        override fun onConnected() {
            connectBlockchainApis(apis)
        }

        override fun onDisconnected() {
            handleCallbackError(SocketException("Socket is disconnected"))
        }
    }

}
