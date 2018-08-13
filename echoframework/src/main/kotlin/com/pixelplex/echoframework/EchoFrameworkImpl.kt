package com.pixelplex.echoframework

import com.pixelplex.echoframework.core.logger.internal.LoggerCoreComponent
import com.pixelplex.echoframework.core.mapper.internal.MapperCoreComponentImpl
import com.pixelplex.echoframework.core.socket.internal.SocketCoreComponentImpl
import com.pixelplex.echoframework.facade.*
import com.pixelplex.echoframework.facade.internal.*
import com.pixelplex.echoframework.model.Account
import com.pixelplex.echoframework.model.Asset
import com.pixelplex.echoframework.model.Balance
import com.pixelplex.echoframework.model.HistoryResponse
import com.pixelplex.echoframework.model.contract.ContractInfo
import com.pixelplex.echoframework.model.contract.ContractMethodParameter
import com.pixelplex.echoframework.model.contract.ContractResult
import com.pixelplex.echoframework.model.contract.ContractStruct
import com.pixelplex.echoframework.service.internal.*
import com.pixelplex.echoframework.support.Settings
import com.pixelplex.echoframework.support.concurrent.Dispatcher
import com.pixelplex.echoframework.support.concurrent.ExecutorServiceDispatcher
import com.pixelplex.echoframework.support.concurrent.MainThreadAccountListener
import com.pixelplex.echoframework.support.concurrent.MainThreadCallback

/**
 * Implementation of [EchoFramework] base library API
 *
 * Delegates all logic to specific facades
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
    private val assetsFacade: AssetsFacade
    private val contractsFacade: ContractsFacade

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

        val accountHistoryApiService = AccountHistoryApiServiceImpl(
            socketCoreComponent,
            settings.network
        )
        val databaseApiService = DatabaseApiServiceImpl(socketCoreComponent, settings.network)
        val networkBroadcastApiService =
            NetworkBroadcastApiServiceImpl(socketCoreComponent, settings.cryptoComponent)
        val cryptoApiService = CryptoApiServiceImpl(socketCoreComponent)
        val loginService = LoginApiServiceImpl(socketCoreComponent)

        initializerFacade = InitializerFacadeImpl(
            socketCoreComponent,
            settings.url,
            settings.apis,
            loginService,
            databaseApiService,
            cryptoApiService,
            accountHistoryApiService,
            networkBroadcastApiService
        )
        authenticationFacade = AuthenticationFacadeImpl(
            databaseApiService,
            networkBroadcastApiService,
            settings.cryptoComponent,
            settings.network
        )
        feeFacade = FeeFacadeImpl(databaseApiService)
        informationFacade = InformationFacadeImpl(databaseApiService, accountHistoryApiService)
        subscriptionFacade = SubscriptionFacadeImpl(
            socketCoreComponent,
            databaseApiService,
            settings.network
        )
        transactionsFacade = TransactionsFacadeImpl(
            databaseApiService,
            networkBroadcastApiService,
            settings.cryptoComponent
        )
        assetsFacade = AssetsFacadeImpl(
            databaseApiService,
            networkBroadcastApiService,
            settings.cryptoComponent
        )
        contractsFacade = ContractsFacadeImpl(
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

    override fun subscribeOnAccount(
        nameOrId: String,
        listener: AccountListener,
        callback: Callback<Boolean>
    ) = dispatch(Runnable {
        subscriptionFacade.subscribeOnAccount(
            nameOrId,
            listener.wrapOriginal(),
            callback.wrapOriginal()
        )
    })

    override fun createAsset(
        name: String,
        password: String,
        asset: Asset,
        callback: Callback<Boolean>
    ) =
        dispatch(Runnable {
            assetsFacade.createAsset(
                name,
                password,
                asset,
                callback
            )
        })

    override fun issueAsset(
        issuerNameOrId: String,
        password: String,
        asset: String,
        amount: String,
        destinationIdOrName: String,
        message: String?,
        callback: Callback<Boolean>
    ) =
        dispatch(Runnable {
            assetsFacade.issueAsset(
                issuerNameOrId,
                password,
                asset,
                amount,
                destinationIdOrName,
                message,
                callback
            )
        })

    override fun listAssets(lowerBound: String, limit: Int, callback: Callback<List<Asset>>) =
        dispatch(Runnable {
            assetsFacade.listAssets(lowerBound, limit, callback)
        })

    override fun getAssets(assetIds: List<String>, callback: Callback<List<Asset>>) =
        dispatch(Runnable {
            assetsFacade.getAssets(assetIds, callback)
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
        message: String?,
        callback: Callback<Boolean>
    ) = dispatch(Runnable {
        transactionsFacade.sendTransferOperation(
            nameOrId,
            password,
            toNameOrId,
            amount,
            asset,
            message,
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

    override fun createContract(
        registrarNameOrId: String,
        password: String,
        assetId: String,
        byteCode: String,
        callback: Callback<Boolean>
    ) = dispatch(Runnable {
        contractsFacade.createContract(
            registrarNameOrId,
            password,
            assetId,
            byteCode,
            callback.wrapOriginal()
        )
    })

    override fun callContract(
        registrarNameOrId: String,
        password: String,
        assetId: String,
        contractId: String,
        methodName: String,
        methodParams: List<ContractMethodParameter>,
        callback: Callback<Boolean>
    ) = dispatch(Runnable {
        contractsFacade.callContract(
            registrarNameOrId,
            password,
            assetId,
            contractId,
            methodName,
            methodParams,
            callback.wrapOriginal()
        )
    })

    override fun queryContract(
        registrarNameOrId: String,
        assetId: String,
        contractId: String,
        methodName: String,
        methodParams: List<ContractMethodParameter>,
        callback: Callback<String>
    ) = dispatch(Runnable {
        contractsFacade.queryContract(
            registrarNameOrId,
            assetId,
            contractId,
            methodName,
            methodParams,
            callback
        )
    })

    override fun getContractResult(
        historyId: String,
        callback: Callback<ContractResult>
    ) = dispatch(Runnable {
        contractsFacade.getContractResult(historyId, callback)
    })

    override fun getContracts(contractIds: List<String>, callback: Callback<List<ContractInfo>>) =
        dispatch(Runnable {
            contractsFacade.getContracts(contractIds, callback)
        })

    override fun getAllContracts(callback: Callback<List<ContractInfo>>) =
        dispatch(Runnable {
            contractsFacade.getAllContracts(callback)
        })

    override fun getContract(contractId: String, callback: Callback<ContractStruct>) =
        dispatch(Runnable {
            contractsFacade.getContract(contractId, callback)
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
