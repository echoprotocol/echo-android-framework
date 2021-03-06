package org.echo.mobile.framework.facade.internal

import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.core.socket.SocketCoreComponent
import org.echo.mobile.framework.core.socket.SocketMessengerListener
import org.echo.mobile.framework.exception.LocalException
import org.echo.mobile.framework.exception.SocketException
import org.echo.mobile.framework.facade.InitializerFacade
import org.echo.mobile.framework.service.AccountHistoryApiService
import org.echo.mobile.framework.service.CryptoApiService
import org.echo.mobile.framework.service.DatabaseApiService
import org.echo.mobile.framework.service.LoginApiService
import org.echo.mobile.framework.service.NetworkBroadcastApiService
import org.echo.mobile.framework.service.RegistrationApiService
import org.echo.mobile.framework.support.Api
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
    private val loginService: LoginApiService,
    private val databaseApiService: DatabaseApiService,
    private val cryptoApiService: CryptoApiService,
    private val accountHistoryApiService: AccountHistoryApiService,
    private val networkBroadcastApiService: NetworkBroadcastApiService,
    private val registrationApiService: RegistrationApiService
) : InitializerFacade {

    private val initializeSocketListener by lazy { InitializeSocketListener() }

    private var connectingCallback: Callback<Any>? = null

    private var apisCount = AtomicInteger(apis.size)

    override fun connect(callback: Callback<Any>) {
        this.connectingCallback = callback

        socketCoreComponent.on(initializeSocketListener)

        try {
            socketCoreComponent.connect(url)
        } catch (e: Exception) {
            callback.onError(LocalException("Error occurred during connection by url = $url", e))
        }
    }

    override fun disconnect() {
        socketCoreComponent.disconnect()
    }

    private fun connectBlockchainApis(apis: Set<Api>) {
        if (apis.isEmpty()) {
            handleCallbackError(SocketException("Cannot find blockchain api for connection")) //?
            return
        }

        loginService.login(object : Callback<Boolean> {
            override fun onSuccess(result: Boolean) {
                apis.forEach { api -> loginService.connectApi(api, ApiCallback(api)) }
            }

            override fun onError(error: LocalException) {
                handleCallbackError(error)
            }
        })
    }

    private fun updateCallback(api: Api, result: Int) {
        updateServiceApiId(api, result)

        val apisLeft = apisCount.decrementAndGet()
        if (apisLeft == 0) {
            connectingCallback?.onSuccess(Any())
            connectingCallback = null
            socketCoreComponent.off(initializeSocketListener)
        }
    }

    private fun updateServiceApiId(api: Api, id: Int) {
        when (api) {
            Api.DATABASE -> databaseApiService.id = id
            Api.ACCOUNT_HISTORY -> accountHistoryApiService.id = id
            Api.CRYPTO -> cryptoApiService.id = id
            Api.NETWORK_BROADCAST -> networkBroadcastApiService.id = id
            Api.REGISTRATION -> registrationApiService.id = id
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


}
