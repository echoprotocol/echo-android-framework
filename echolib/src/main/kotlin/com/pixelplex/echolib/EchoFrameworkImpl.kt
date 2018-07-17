package com.pixelplex.echolib

import com.pixelplex.echolib.core.crypto.internal.CryptoCoreComponentImpl
import com.pixelplex.echolib.core.mapper.internal.MapperCoreComponentImpl
import com.pixelplex.echolib.core.socket.internal.SocketCoreComponentImpl
import com.pixelplex.echolib.facade.*
import com.pixelplex.echolib.facade.internal.*
import com.pixelplex.echolib.model.Account
import com.pixelplex.echolib.model.Balance
import com.pixelplex.echolib.model.HistoryResponse
import com.pixelplex.echolib.service.internal.AccountHistoryApiServiceImpl
import com.pixelplex.echolib.service.internal.DatabaseApiServiceImpl
import com.pixelplex.echolib.service.internal.NetworkBroadcastApiServiceImpl
import com.pixelplex.echolib.support.Settings
import com.pixelplex.echolib.support.concurrent.Dispatcher
import com.pixelplex.echolib.support.concurrent.ExecutorServiceDispatcher
import com.pixelplex.echolib.support.concurrent.MainThreadCallback

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
        val cryptoCoreComponent =
            CryptoCoreComponentImpl()
        val socketCoreComponent =
            SocketCoreComponentImpl(settings.socketMessenger, mapperCoreComponent)

        val accountHistoryApiService =
            AccountHistoryApiServiceImpl(socketCoreComponent, settings.cryptoComponent)
        val databaseApiService = DatabaseApiServiceImpl(socketCoreComponent)
        val networkBroadcastApiService = NetworkBroadcastApiServiceImpl(socketCoreComponent)

        initializerFacade = InitializerFacadeImpl(socketCoreComponent, settings.url, settings.apis)
        authenticationFacade = AuthenticationFacadeImpl(databaseApiService, cryptoCoreComponent)
        feeFacade = FeeFacadeImpl(databaseApiService)
        informationFacade = InformationFacadeImpl(databaseApiService)
        subscriptionFacade =
                SubscriptionFacadeImpl(networkBroadcastApiService)
        transactionsFacade =
                TransactionsFacadeImpl(networkBroadcastApiService, accountHistoryApiService)
    }

    override fun start(callback: Callback<Any>) {
        val threadKeepCallback = callback.wrapOriginal()
        dispatch(Runnable { initializerFacade.connect(threadKeepCallback) })
    }

    override fun stop() {

    }

    override fun login(name: String, password: String, callback: Callback<Account>) {
        val threadKeepCallback = callback.wrapOriginal()
        dispatch(Runnable {
            authenticationFacade.login(
                name,
                password,
                threadKeepCallback
            )
        })
    }

    override fun changePassword(
        nameOrId: String,
        oldPassword: String,
        newPassword: String,
        callback: Callback<Account>
    ) {
        val threadKeepCallback = callback.wrapOriginal()
        dispatch(Runnable {
            authenticationFacade.changePassword(
                nameOrId,
                oldPassword,
                newPassword,
                threadKeepCallback
            )
        })
    }

    override fun getFeeForTransferOperation(
        fromNameOrId: String,
        toNameOrId: String,
        asset: String,
        callback: Callback<String>
    ) {
        val threadKeepCallback = callback.wrapOriginal()
        dispatch(Runnable {
            feeFacade.getFeeForTransferOperation(
                fromNameOrId,
                toNameOrId,
                asset,
                threadKeepCallback
            )
        })
    }

    override fun getAccount(nameOrId: String, callback: Callback<Account>) {
        val threadKeepCallback = callback.wrapOriginal()
        dispatch(Runnable {
            informationFacade.getAccount(nameOrId, threadKeepCallback)
        })
    }

    override fun checkAccountIsUnavailable(nameOrId: String, callback: Callback<Boolean>) {
        val threadKeepCallback = callback.wrapOriginal()
        dispatch(Runnable {
            informationFacade.checkAccountIsUnavailable(nameOrId, threadKeepCallback)
        })
    }

    override fun getBalance(nameOrId: String, asset: String, callback: Callback<Balance>) {
        val threadKeepCallback = callback.wrapOriginal()
        dispatch(Runnable {
            informationFacade.getBalance(nameOrId, asset, threadKeepCallback)
        })
    }

    override fun subscribeOnAccount(nameOrId: String, listener: AccountListener) {
        subscriptionFacade.subscribeOnAccount(nameOrId, listener)
    }

    override fun unsubscribeFromAccount(nameOrId: String, callback: Callback<Boolean>) =
        subscriptionFacade.unsubscribeFromAccount(nameOrId, callback)

    override fun unsubscribeAll(callback: Callback<Boolean>) =
        subscriptionFacade.unsubscribeAll(callback)

    override fun sendTransferOperation(
        nameOrId: String,
        password: String,
        toNameOrId: String,
        amount: String,
        asset: String,
        callback: Callback<String>
    ) {
        val threadKeepCallback = callback.wrapOriginal()
        dispatch(Runnable {
            transactionsFacade.sendTransferOperation(
                nameOrId,
                password,
                toNameOrId,
                amount,
                asset,
                threadKeepCallback
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
        val threadKeepCallback = callback.wrapOriginal()
        dispatch(Runnable {
            transactionsFacade.getAccountHistory(
                nameOrId,
                transactionStartId,
                transactionStopId,
                limit,
                asset,
                threadKeepCallback
            )
        })
    }

    private fun <T> Callback<T>.wrapOriginal(): Callback<T> {
        if (!returnOnMainThread) {
            return this
        }
        return MainThreadCallback(this)
    }

    private fun dispatch(job: Runnable) = dispatcher.dispatch(job)

}
