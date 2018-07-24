package com.pixelplex.echoframework

import com.pixelplex.echoframework.core.mapper.internal.MapperCoreComponentImpl
import com.pixelplex.echoframework.core.socket.internal.SocketCoreComponentImpl
import com.pixelplex.echoframework.facade.*
import com.pixelplex.echoframework.facade.internal.*
import com.pixelplex.echoframework.model.Account
import com.pixelplex.echoframework.model.Balance
import com.pixelplex.echoframework.model.HistoryResponse
import com.pixelplex.echoframework.service.internal.AccountHistoryApiServiceImpl
import com.pixelplex.echoframework.service.internal.CryptoApiServiceImpl
import com.pixelplex.echoframework.service.internal.DatabaseApiServiceImpl
import com.pixelplex.echoframework.service.internal.NetworkBroadcastApiServiceImpl
import com.pixelplex.echoframework.support.Settings
import com.pixelplex.echoframework.support.concurrent.Dispatcher
import com.pixelplex.echoframework.support.concurrent.ExecutorServiceDispatcher
import com.pixelplex.echoframework.support.concurrent.MainThreadAccountListener
import com.pixelplex.echoframework.support.concurrent.MainThreadCallback

/**
 * Implementation of [EchoFramework] base library API
 *
 * <p>
 *     Delegates all logic to specific facades
 * </p>
 *
 * @author Dmitriy Bushuev
 */
class EchoFrameworkImpl internal constructor(settings: Settings) : EchoFramework {

    private val initializerFacade: InitializerFacade
    private val authenticationFacade: AuthenticationFacade
    private val feeFacade: FeeFacade
    private val informationFacade: InformationFacade
    private val subscriptionFacade: SubscriptionFacade
    private val transactionsFacade: TransactionsFacade

    // move calls dispatching logic on another layer?
    private val dispatcher: Dispatcher by lazy { ExecutorServiceDispatcher() }
    private var returnOnMainThread = false

    /**
     * Initializes and setups all facades with required dependencies
     */
    init {
        returnOnMainThread = settings.returnOnMainThread

        val mapperCoreComponent =
            MapperCoreComponentImpl()
        val socketCoreComponent =
            SocketCoreComponentImpl(settings.socketMessenger, mapperCoreComponent)

        val accountHistoryApiService =
            AccountHistoryApiServiceImpl(
                socketCoreComponent,
                settings.cryptoComponent,
                settings.network
            )
        val databaseApiService = DatabaseApiServiceImpl(socketCoreComponent, settings.network)
        val networkBroadcastApiService =
            NetworkBroadcastApiServiceImpl(socketCoreComponent, settings.cryptoComponent)
        val cryptoApiService = CryptoApiServiceImpl(socketCoreComponent)

        initializerFacade = InitializerFacadeImpl(
            socketCoreComponent,
            settings.url,
            settings.apis,
            databaseApiService,
            cryptoApiService,
            accountHistoryApiService,
            networkBroadcastApiService
        )
        authenticationFacade =
                AuthenticationFacadeImpl(
                    databaseApiService,
                    networkBroadcastApiService,
                    settings.cryptoComponent,
                    settings.network
                )
        feeFacade = FeeFacadeImpl(databaseApiService)
        informationFacade = InformationFacadeImpl(databaseApiService)
        subscriptionFacade =
                SubscriptionFacadeImpl(databaseApiService)
        transactionsFacade =
                TransactionsFacadeImpl(
                    databaseApiService,
                    networkBroadcastApiService,
                    settings.cryptoComponent,
                    accountHistoryApiService
                )
    }

    override fun start(callback: Callback<Any>) {
        dispatch(Runnable { initializerFacade.connect(callback.wrapOriginal()) })
    }

    override fun stop() {

    }

    override fun login(name: String, password: String, callback: Callback<Account>) {
        dispatch(Runnable {
            authenticationFacade.login(
                name,
                password,
                callback.wrapOriginal()
            )
        })
    }

    override fun changePassword(
        name: String,
        oldPassword: String,
        newPassword: String,
        callback: Callback<Any>
    ) {
        dispatch(Runnable {
            authenticationFacade.changePassword(
                name,
                oldPassword,
                newPassword,
                callback.wrapOriginal()
            )
        })
    }

    override fun getFeeForTransferOperation(
        fromNameOrId: String,
        toNameOrId: String,
        asset: String,
        callback: Callback<String>
    ) {
        dispatch(Runnable {
            feeFacade.getFeeForTransferOperation(
                fromNameOrId,
                toNameOrId,
                asset,
                callback.wrapOriginal()
            )
        })
    }

    override fun getAccount(nameOrId: String, callback: Callback<Account>) {
        val threadKeepCallback = callback.wrapOriginal()
        dispatch(Runnable {
            informationFacade.getAccount(nameOrId, threadKeepCallback)
        })
    }

    override fun checkAccountReserved(nameOrId: String, callback: Callback<Boolean>) {
        dispatch(Runnable {
            informationFacade.checkAccountReserved(nameOrId, callback.wrapOriginal())
        })
    }

    override fun getBalance(nameOrId: String, asset: String, callback: Callback<Balance>) {
        dispatch(Runnable {
            informationFacade.getBalance(nameOrId, asset, callback.wrapOriginal())
        })
    }

    override fun subscribeOnAccount(id: String, listener: AccountListener) {
        dispatch(Runnable {
            subscriptionFacade.subscribeOnAccount(id, listener.wrapOriginal())
        })
    }

    override fun unsubscribeFromAccount(id: String, callback: Callback<Boolean>) =
        dispatch(Runnable {
            subscriptionFacade.unsubscribeFromAccount(id, callback.wrapOriginal())
        })

    override fun unsubscribeAll(callback: Callback<Boolean>) =
        dispatch(Runnable {
            subscriptionFacade.unsubscribeAll(callback.wrapOriginal())
        })

    override fun sendTransferOperation(
        nameOrId: String,
        password: String,
        toNameOrId: String,
        amount: String,
        asset: String,
        callback: Callback<String>
    ) {
        dispatch(Runnable {
            transactionsFacade.sendTransferOperation(
                nameOrId,
                password,
                toNameOrId,
                amount,
                asset,
                callback.wrapOriginal()
            )
        })
    }

    override fun getAccountHistory(
        nameOrId: String,
        transactionStartId: String,
        transactionStopId: String,
        limit: Int,
        asset: String,
        callback: Callback<HistoryResponse>
    ) {
        dispatch(Runnable {
            transactionsFacade.getAccountHistory(
                nameOrId,
                transactionStartId,
                transactionStopId,
                limit,
                asset,
                callback.wrapOriginal()
            )
        })
    }

    private fun <T> Callback<T>.wrapOriginal(): Callback<T> {
        if (!returnOnMainThread) {
            return this
        }
        return MainThreadCallback(this)
    }

    private fun AccountListener.wrapOriginal(): AccountListener {
        if (!returnOnMainThread) {
            return this
        }
        return MainThreadAccountListener(this)
    }

    private fun dispatch(job: Runnable) = dispatcher.dispatch(job)

}
