package com.pixelplex.echoframework

import com.pixelplex.echoframework.core.logger.internal.LoggerCoreComponent
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

    private val dispatcher: Dispatcher by lazy { ExecutorServiceDispatcher() }
    private var returnOnMainThread = false

    /**
     * Initializes and setups all facades with required dependencies
     */
    init {
        LoggerCoreComponent.logLevel = settings.logLevel
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
        informationFacade = InformationFacadeImpl(databaseApiService, accountHistoryApiService)
        subscriptionFacade =
                SubscriptionFacadeImpl(databaseApiService)
        transactionsFacade =
                TransactionsFacadeImpl(
                    databaseApiService,
                    networkBroadcastApiService,
                    settings.cryptoComponent
                )
    }

    override fun start(callback: Callback<Any>) =
        dispatch(Runnable { initializerFacade.connect(callback.wrapOriginal()) })

    override fun stop() =
        dispatch(Runnable { initializerFacade.disconnect() })

    override fun isOwnedBy(name: String, password: String, callback: Callback<Account>) =
        dispatch(Runnable {
            authenticationFacade.isOwnedBy(
                name,
                password,
                callback.wrapOriginal()
            )
        })

    override fun changePassword(
        name: String,
        oldPassword: String,
        newPassword: String,
        callback: Callback<Any>
    ) = dispatch(Runnable {
        authenticationFacade.changePassword(
            name,
            oldPassword,
            newPassword,
            callback.wrapOriginal()
        )
    })

    override fun getFeeForTransferOperation(
        fromNameOrId: String,
        toNameOrId: String,
        amount: String,
        asset: String,
        callback: Callback<String>
    ) = dispatch(Runnable {
        feeFacade.getFeeForTransferOperation(
            fromNameOrId,
            toNameOrId,
            amount,
            asset,
            callback.wrapOriginal()
        )
    })

    override fun getAccount(nameOrId: String, callback: Callback<Account>) =
        dispatch(Runnable {
            informationFacade.getAccount(nameOrId, callback.wrapOriginal())
        })

    override fun checkAccountReserved(nameOrId: String, callback: Callback<Boolean>) =
        dispatch(Runnable {
            informationFacade.checkAccountReserved(nameOrId, callback.wrapOriginal())
        })

    override fun getBalance(nameOrId: String, asset: String, callback: Callback<Balance>) =
        dispatch(Runnable {
            informationFacade.getBalance(nameOrId, asset, callback.wrapOriginal())
        })

    override fun subscribeOnAccount(nameOrId: String, listener: AccountListener) =
        dispatch(Runnable {
            subscriptionFacade.subscribeOnAccount(nameOrId, listener.wrapOriginal())
        })

    override fun unsubscribeFromAccount(nameOrId: String, callback: Callback<Boolean>) =
        dispatch(Runnable {
            subscriptionFacade.unsubscribeFromAccount(nameOrId, callback.wrapOriginal())
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
        callback: Callback<Boolean>
    ) = dispatch(Runnable {
        transactionsFacade.sendTransferOperation(
            nameOrId,
            password,
            toNameOrId,
            amount,
            asset,
            callback.wrapOriginal()
        )
    })

    override fun getAccountHistory(
        nameOrId: String,
        transactionStartId: String,
        transactionStopId: String,
        limit: Int,
        asset: String,
        callback: Callback<HistoryResponse>
    ) = dispatch(Runnable {
        informationFacade.getAccountHistory(
            nameOrId,
            transactionStartId,
            transactionStopId,
            limit,
            asset,
            callback.wrapOriginal()
        )
    })

    private fun <T> Callback<T>.wrapOriginal(): Callback<T> =
        if (!returnOnMainThread) {
            this
        } else {
            MainThreadCallback(this)
        }

    private fun AccountListener.wrapOriginal(): AccountListener =
        if (!returnOnMainThread) {
            this
        } else {
            MainThreadAccountListener(this)
        }

    private fun dispatch(job: Runnable) = dispatcher.dispatch(job)

}
