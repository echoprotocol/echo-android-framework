package com.pixelplex.echoframework.facade.internal

import com.pixelplex.echoframework.Callback
import com.pixelplex.echoframework.core.socket.SocketCoreComponent
import com.pixelplex.echoframework.core.socket.SocketMessengerListener
import com.pixelplex.echoframework.exception.LocalException
import com.pixelplex.echoframework.exception.SocketException
import com.pixelplex.echoframework.facade.InitializerFacade
import com.pixelplex.echoframework.model.socketoperations.AccessSocketOperation
import com.pixelplex.echoframework.model.socketoperations.AccessSocketOperationType
import com.pixelplex.echoframework.model.socketoperations.LoginSocketOperation
import com.pixelplex.echoframework.model.socketoperations.SocketOperation
import com.pixelplex.echoframework.service.AccountHistoryApiService
import com.pixelplex.echoframework.service.CryptoApiService
import com.pixelplex.echoframework.service.DatabaseApiService
import com.pixelplex.echoframework.service.NetworkBroadcastApiService
import com.pixelplex.echoframework.support.Api
import com.pixelplex.echoframework.support.Converter
import java.util.concurrent.atomic.AtomicInteger

/**
 * Implementation of [InitializerFacade]
 *
 * @author Daria Pechkovskaya
 */
class InitializerFacadeImpl(
    private val socketCoreComponent: SocketCoreComponent,
    private val url: String,
    private val apis: Set<Api>,
    private val databaseApiService: DatabaseApiService,
    private val cryptoApiService: CryptoApiService,
    private val accountHistoryApiService: AccountHistoryApiService,
    private val networkBroadcastApiService: NetworkBroadcastApiService
) : InitializerFacade {

    private val initializeSocketListener by lazy { InitializeSocketListener() }

    private var connectingCallback: Callback<Any>? = null

    private var apisCount = AtomicInteger(apis.size)

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

        login(object : Callback<Boolean> {
            override fun onSuccess(result: Boolean) {
                val apisOperations = createApiOperations(apis)
                apisOperations.forEach { operation -> socketCoreComponent.emit(operation) }
            }

            override fun onError(error: LocalException) {
                handleCallbackError(error)
            }
        })
    }

    private fun login(callback: Callback<Boolean>) {
        val loginOperation = LoginSocketOperation(
            InitializerFacade.INITIALIZER_API_ID,
            callback = callback
        )

        socketCoreComponent.emit(loginOperation)
    }

    private fun createApiOperations(apis: Set<Api>): List<SocketOperation<*>> {
        val operations = arrayListOf<SocketOperation<*>>()
        val apiTypeConverter = ApiToOperationTypeConverter()

        operations.addAll(apis.map { api ->
            AccessSocketOperation(
                accessSocketType = apiTypeConverter.convert(api),
                api = InitializerFacade.INITIALIZER_API_ID,
                callback = ApiCallback(api)
            )
        })

        return operations
    }

    private fun updateCallback(api: Api, result: Int) {
        updateServiceApiId(api, result)

        val apisLeft = apisCount.decrementAndGet()
        if (apisLeft == 0) {
            synchronized(this) {
                if (apisLeft == 0) {
                    connectingCallback?.onSuccess(Any())
                    connectingCallback = null
                    socketCoreComponent.off(initializeSocketListener)
                }
            }
        }
    }

    private fun updateServiceApiId(api: Api, id: Int) {
        when (api) {
            Api.DATABASE -> databaseApiService.id = id
            Api.ACCOUNT_HISTORY -> accountHistoryApiService.id = id
            Api.CRYPTO -> cryptoApiService.id = id
            Api.NETWORK_BROADCAST -> networkBroadcastApiService.id = id
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

    private inner class ApiCallback(private val api: Api) : Callback<Int> {

        override fun onSuccess(result: Int) {
            updateCallback(api, result)
        }

        override fun onError(error: LocalException) {
            handleCallbackError(error)
        }

    }

    private class ApiToOperationTypeConverter : Converter<Api, AccessSocketOperationType> {

        private val apiToType = hashMapOf(
            Api.DATABASE to AccessSocketOperationType.DATABASE,
            Api.NETWORK_BROADCAST to AccessSocketOperationType.NETWORK_BROADCAST,
            Api.ACCOUNT_HISTORY to AccessSocketOperationType.HISTORY,
            Api.CRYPTO to AccessSocketOperationType.CRYPTO
        )

        override fun convert(source: Api): AccessSocketOperationType =
            apiToType[source] ?: throw IllegalArgumentException("Unrecognized api type: $source")
    }

}
