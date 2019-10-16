package org.echo.mobile.framework

import org.echo.mobile.framework.core.crypto.internal.eddsa.EdDSASecurityProvider
import org.echo.mobile.framework.core.logger.internal.LoggerCoreComponent
import org.echo.mobile.framework.core.mapper.internal.MapperCoreComponentImpl
import org.echo.mobile.framework.core.socket.internal.SocketCoreComponentImpl
import org.echo.mobile.framework.facade.*
import org.echo.mobile.framework.facade.internal.*
import org.echo.mobile.framework.model.*
import org.echo.mobile.framework.model.contract.*
import org.echo.mobile.framework.model.contract.input.InputValue
import org.echo.mobile.framework.service.*
import org.echo.mobile.framework.service.internal.*
import org.echo.mobile.framework.service.internal.subscription.RegistrationSubscriptionManagerImpl
import org.echo.mobile.framework.service.internal.subscription.TransactionSubscriptionManagerImpl
import org.echo.mobile.framework.support.FeeRatioProvider
import org.echo.mobile.framework.support.Settings
import org.echo.mobile.framework.support.concurrent.*
import java.security.Security

/**
 * Implementation of [EchoFramework] base library API
 *
 * Delegates all logic to specific facades
 *
 * All methods and services can lead to error if you use them without associated initialized api id,
 * that eou need to specify in [Settings] before library initialization
 *
 * @author Dmitriy Bushuev
 */
class EchoFrameworkImpl internal constructor(settings: Settings) : EchoFramework {

    override val accountHistoryApiService: AccountHistoryApiService
    override val databaseApiService: DatabaseApiService
    override val networkBroadcastApiService: NetworkBroadcastApiService
    override val cryptoApiService: CryptoApiService
    override val loginService: LoginApiService
    override val registrationService: RegistrationApiService

    private val initializerFacade: InitializerFacade
    private val authenticationFacade: AuthenticationFacade
    private val feeFacade: FeeFacade
    private val informationFacade: InformationFacade
    private val subscriptionFacade: SubscriptionFacade
    private val transactionsFacade: TransactionsFacade
    private val assetsFacade: AssetsFacade
    private val contractsFacade: ContractsFacade
    private val sidechainFacade: SidechainFacade

    private val dispatcher: Dispatcher by lazy { ExecutorServiceDispatcher() }
    private var returnOnMainThread = false

    /**
     * Initializes and setups all facades with required dependencies
     */
    init {
        Security.addProvider(EdDSASecurityProvider())

        LoggerCoreComponent.logLevel = settings.logLevel
        returnOnMainThread = settings.returnOnMainThread

        val mapperCoreComponent = MapperCoreComponentImpl()
        val socketCoreComponent =
            SocketCoreComponentImpl(settings.socketMessenger, mapperCoreComponent)

        accountHistoryApiService = AccountHistoryApiServiceImpl(
            socketCoreComponent,
            settings.network
        )
        databaseApiService =
            DatabaseApiServiceImpl(socketCoreComponent, settings.cryptoComponent, settings.network)
        networkBroadcastApiService =
            NetworkBroadcastApiServiceImpl(socketCoreComponent, settings.cryptoComponent)
        cryptoApiService = CryptoApiServiceImpl(socketCoreComponent)
        loginService = LoginApiServiceImpl(socketCoreComponent)
        registrationService = RegistrationApiServiceImpl(socketCoreComponent)

        initializerFacade = InitializerFacadeImpl(
            socketCoreComponent,
            settings.url,
            settings.apis,
            loginService,
            databaseApiService,
            cryptoApiService,
            accountHistoryApiService,
            networkBroadcastApiService,
            registrationService
        )

        val regularSubscriptionManager = RegistrationSubscriptionManagerImpl()
        val registrationNotificationsHelper = NotificationsHelper(
            socketCoreComponent,
            regularSubscriptionManager
        )

        authenticationFacade = AuthenticationFacadeImpl(
            databaseApiService,
            networkBroadcastApiService,
            registrationService,
            settings.cryptoComponent,
            registrationNotificationsHelper
        )

        val feeRatioProvider = FeeRatioProvider(settings.feeRatio)

        feeFacade = FeeFacadeImpl(databaseApiService, settings.cryptoComponent, feeRatioProvider)
        informationFacade = InformationFacadeImpl(
            databaseApiService,
            accountHistoryApiService
        )
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

        val transactionSubscriptionManager = TransactionSubscriptionManagerImpl(settings.network)

        val notifiedTransactionsHelper =
            NotificationsHelper(socketCoreComponent, transactionSubscriptionManager)

        assetsFacade = AssetsFacadeImpl(
            databaseApiService,
            networkBroadcastApiService,
            settings.cryptoComponent,
            notifiedTransactionsHelper
        )
        contractsFacade = ContractsFacadeImpl(
            databaseApiService,
            networkBroadcastApiService,
            settings.cryptoComponent,
            notifiedTransactionsHelper,
            feeRatioProvider
        )

        val notifiedEthAddressHelper =
            NotificationsHelper(socketCoreComponent, transactionSubscriptionManager)
        sidechainFacade = SidechainFacadeImpl(
            databaseApiService,
            networkBroadcastApiService,
            settings.cryptoComponent,
            notifiedEthAddressHelper
        )
    }

    override fun start(callback: Callback<Any>) =
        dispatch(Runnable { initializerFacade.connect(callback.wrapOriginal()) })

    override fun stop() =
        dispatch(Runnable { initializerFacade.disconnect() })

    override fun isOwnedBy(nameOrId: String, wif: String, callback: Callback<FullAccount>) =
        dispatch(Runnable {
            authenticationFacade.isOwnedBy(
                nameOrId,
                wif,
                callback.wrapOriginal()
            )
        })

    override fun changeKeys(
        name: String,
        oldWif: String,
        newWif: String,
        callback: Callback<Any>
    ) = dispatch(Runnable {
        authenticationFacade.changeKeys(
            name,
            oldWif,
            newWif,
            callback.wrapOriginal()
        )
    })

    override fun register(userName: String, wif: String, callback: Callback<Boolean>) =
        dispatch(Runnable {
            authenticationFacade.register(
                userName,
                wif,
                callback.wrapOriginal()
            )
        })

    override fun getFeeForTransferOperation(
        fromNameOrId: String,
        wif: String,
        toNameOrId: String,
        amount: String,
        asset: String,
        feeAsset: String?,
        callback: Callback<String>
    ) = dispatch(Runnable {
        feeFacade.getFeeForTransferOperation(
            fromNameOrId,
            wif,
            toNameOrId,
            amount,
            asset,
            feeAsset,
            callback.wrapOriginal()
        )
    })

    override fun getFeeForContractOperation(
        userNameOrId: String,
        contractId: String,
        amount: String,
        methodName: String,
        methodParams: List<InputValue>,
        assetId: String,
        feeAsset: String?,
        callback: Callback<ContractFee>
    ) = dispatch(Runnable {
        feeFacade.getFeeForContractOperation(
            userNameOrId,
            contractId,
            amount,
            methodName,
            methodParams,
            assetId,
            feeAsset,
            callback.wrapOriginal()
        )
    })

    override fun getFeeForContractOperation(
        userNameOrId: String,
        contractId: String,
        amount: String,
        code: String,
        assetId: String,
        feeAsset: String?,
        callback: Callback<ContractFee>
    ) = dispatch(Runnable {
        feeFacade.getFeeForContractOperation(
            userNameOrId,
            contractId,
            amount,
            code,
            assetId,
            feeAsset,
            callback.wrapOriginal()
        )
    })

    override fun getAccount(nameOrId: String, callback: Callback<FullAccount>) =
        dispatch(Runnable {
            informationFacade.getAccount(nameOrId, callback.wrapOriginal())
        })

    override fun getAccountsByWif(wif: String, callback: Callback<List<FullAccount>>) =
        dispatch(Runnable {
            informationFacade.getAccountsByWif(wif, callback.wrapOriginal())
        })

    override fun checkAccountReserved(
        nameOrId: String,
        callback: Callback<Boolean>
    ) =
        dispatch(Runnable {
            informationFacade.checkAccountReserved(nameOrId, callback.wrapOriginal())
        })

    override fun getBalance(
        nameOrId: String,
        asset: String,
        callback: Callback<Balance>
    ) =
        dispatch(Runnable {
            informationFacade.getBalance(nameOrId, asset, callback.wrapOriginal())
        })

    override fun getGlobalProperties(callback: Callback<GlobalProperties>) =
        dispatch(Runnable {
            informationFacade.getGlobalProperties(callback.wrapOriginal())
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

    override fun subscribeOnBlock(
        listener: UpdateListener<Block>,
        callback: Callback<Boolean>
    ) =
        dispatch(Runnable {
            subscriptionFacade.subscribeOnBlock(
                listener.wrapOriginal(),
                callback.wrapOriginal()
            )
        })

    override fun subscribeOnBlockchainData(
        listener: UpdateListener<DynamicGlobalProperties>,
        callback: Callback<Boolean>
    ) = dispatch(Runnable {
        subscriptionFacade.subscribeOnBlockchainData(
            listener.wrapOriginal(),
            callback.wrapOriginal()
        )
    })

    override fun subscribeOnContractLogs(
        contractId: String,
        listener: UpdateListener<List<ContractLog>>,
        callback: Callback<Boolean>
    ) = dispatch(Runnable {
        subscriptionFacade.subscribeOnContractLogs(contractId, listener, callback)
    })

    override fun subscribeOnContracts(
        contractIds: List<String>,
        listener: UpdateListener<Map<String, List<ContractBalance>>>,
        callback: Callback<Boolean>
    ) = dispatch(Runnable {
        subscriptionFacade.subscribeOnContracts(contractIds, listener, callback)
    })

    override fun unsubscribeFromContractLogs(
        contractId: String,
        callback: Callback<Boolean>
    ) =
        dispatch(Runnable {
            subscriptionFacade.unsubscribeFromContractLogs(
                contractId,
                callback.wrapOriginal()
            )
        })

    override fun unsubscribeFromContracts(
        listener: UpdateListener<Map<String, List<ContractBalance>>>,
        callback: Callback<Boolean>
    ) =
        dispatch(Runnable {
            subscriptionFacade.unsubscribeFromContracts(
                listener,
                callback.wrapOriginal()
            )
        })

    override fun unsubscribeFromBlockchainData(callback: Callback<Boolean>) =
        dispatch(Runnable {
            subscriptionFacade.unsubscribeFromBlockchainData(callback)
        })

    override fun unsubscribeFromBlock(callback: Callback<Boolean>) =
        dispatch(Runnable {
            subscriptionFacade.unsubscribeFromBlock(callback)
        })

    override fun createAsset(
        name: String,
        wif: String,
        asset: Asset,
        broadcastCallback: Callback<Boolean>,
        resultCallback: Callback<String>?
    ) = dispatch(Runnable {
        assetsFacade.createAsset(
            name, wif,
            asset,
            broadcastCallback.wrapOriginal(), resultCallback?.wrapOriginal()
        )
    })

    override fun issueAsset(
        issuerNameOrId: String,
        wif: String,
        asset: String,
        amount: String,
        destinationIdOrName: String,
        callback: Callback<Boolean>
    ) = dispatch(Runnable {
        assetsFacade.issueAsset(
            issuerNameOrId,
            wif,
            asset,
            amount,
            destinationIdOrName,
            callback
        )
    })

    override fun listAssets(
        lowerBound: String,
        limit: Int,
        callback: Callback<List<Asset>>
    ) =
        dispatch(Runnable {
            assetsFacade.listAssets(lowerBound, limit, callback)
        })

    override fun getAssets(
        assetIds: List<String>,
        callback: Callback<List<Asset>>
    ) =
        dispatch(Runnable {
            assetsFacade.getAssets(assetIds, callback)
        })

    override fun lookupAssetsSymbols(symbolsOrIds: List<String>, callback: Callback<List<Asset>>) =
        dispatch(Runnable {
            assetsFacade.lookupAssetsSymbols(symbolsOrIds, callback)
        })

    override fun unsubscribeFromAccount(
        nameOrId: String,
        callback: Callback<Boolean>
    ) =
        dispatch(Runnable {
            subscriptionFacade.unsubscribeFromAccount(
                nameOrId,
                callback.wrapOriginal()
            )
        })

    override fun unsubscribeAll(callback: Callback<Boolean>) =
        dispatch(Runnable {
            subscriptionFacade.unsubscribeAll(callback.wrapOriginal())
        })

    override fun sendTransferOperation(
        nameOrId: String,
        wif: String,
        toNameOrId: String,
        amount: String,
        asset: String,
        feeAsset: String?,
        callback: Callback<Boolean>
    ) {
        transactionsFacade.sendTransferOperation(
            nameOrId,
            wif,
            toNameOrId,
            amount,
            asset,
            feeAsset,
            callback.wrapOriginal()
        )
    }

    override fun getAccountHistory(
        nameOrId: String,
        transactionStartId: String,
        transactionStopId: String,
        limit: Int,
        callback: Callback<HistoryResponse>
    ) = dispatch(Runnable {
        informationFacade.getAccountHistory(
            nameOrId,
            transactionStartId,
            transactionStopId,
            limit,
            callback.wrapOriginal()
        )
    })

    override fun createContract(
        registrarNameOrId: String,
        wif: String,
        value: String,
        assetId: String,
        feeAsset: String?,
        byteCode: String,
        params: List<InputValue>,
        broadcastCallback: Callback<Boolean>,
        resultCallback: Callback<String>?
    ) = dispatch(Runnable {
        contractsFacade.createContract(
            registrarNameOrId,
            wif,
            value,
            assetId,
            feeAsset,
            byteCode,
            params,
            broadcastCallback.wrapOriginal(),
            resultCallback?.wrapOriginal()
        )
    })

    override fun callContract(
        userNameOrId: String,
        wif: String,
        assetId: String,
        feeAsset: String?,
        contractId: String,
        methodName: String,
        methodParams: List<InputValue>,
        value: String,
        broadcastCallback: Callback<Boolean>,
        resultCallback: Callback<String>?
    ) = dispatch(Runnable {
        contractsFacade.callContract(
            userNameOrId,
            wif,
            assetId,
            feeAsset,
            contractId,
            methodName,
            methodParams,
            value,
            broadcastCallback.wrapOriginal(),
            resultCallback?.wrapOriginal()
        )
    })

    override fun callContract(
        userNameOrId: String,
        wif: String,
        assetId: String,
        feeAsset: String?,
        contractId: String,
        code: String,
        value: String,
        broadcastCallback: Callback<Boolean>,
        resultCallback: Callback<String>?
    ) = dispatch(Runnable {
        contractsFacade.callContract(
            userNameOrId,
            wif,
            assetId,
            feeAsset,
            contractId,
            code,
            value,
            broadcastCallback.wrapOriginal(),
            resultCallback?.wrapOriginal()
        )
    })

    override fun queryContract(
        userNameOrId: String,
        assetId: String,
        contractId: String,
        methodName: String,
        methodParams: List<InputValue>,
        callback: Callback<String>
    ) = dispatch(Runnable {
        contractsFacade.queryContract(
            userNameOrId,
            assetId,
            contractId,
            methodName,
            methodParams,
            callback
        )
    })

    override fun queryContract(
        userNameOrId: String,
        assetId: String,
        contractId: String,
        code: String,
        callback: Callback<String>
    ) = dispatch(Runnable {
        contractsFacade.queryContract(
            userNameOrId,
            assetId,
            contractId,
            code,
            callback
        )
    })

    override fun getContractResult(
        historyId: String,
        callback: Callback<ContractResult>
    ) = dispatch(Runnable {
        contractsFacade.getContractResult(
            historyId,
            callback
        )
    })

    override fun getContractLogs(
        contractId: String,
        fromBlock: String,
        toBlock: String,
        callback: Callback<List<ContractLog>>
    ) = dispatch(Runnable {
        contractsFacade.getContractLogs(
            contractId, fromBlock, toBlock, callback
        )
    })

    override fun generateEthereumAddress(
        accountNameOrId: String,
        wif: String,
        broadcastCallback: Callback<Boolean>,
        resultCallback: Callback<TransactionResult>?
    ) =
        dispatch(Runnable {
            sidechainFacade.generateEthereumAddress(
                accountNameOrId, wif, broadcastCallback, resultCallback
            )
        })

    override fun ethWithdraw(
        accountNameOrId: String,
        wif: String,
        ethAddress: String,
        value: String,
        feeAsset: String,
        broadcastCallback: Callback<Boolean>,
        resultCallback: Callback<TransactionResult>?
    ) =
        dispatch(Runnable {
            sidechainFacade.ethWithdraw(
                accountNameOrId,
                wif,
                ethAddress,
                value,
                feeAsset,
                broadcastCallback,
                resultCallback
            )
        })

    override fun getEthereumAddress(
        accountNameOrId: String,
        callback: Callback<EthAddress>
    ) =
        dispatch(Runnable {
            sidechainFacade.getEthereumAddress(accountNameOrId, callback)
        })

    override fun getAccountDeposits(accountNameOrId: String, callback: Callback<List<EthDeposit>>) =
        dispatch(Runnable {
            sidechainFacade.getAccountDeposits(accountNameOrId, callback)
        })

    override fun getAccountWithdrawals(
        accountNameOrId: String,
        callback: Callback<List<EthWithdraw>>
    ) =
        dispatch(Runnable {
            sidechainFacade.getAccountWithdrawals(accountNameOrId, callback)
        })

    override fun getContracts(
        contractIds: List<String>,
        callback: Callback<List<ContractInfo>>
    ) =
        dispatch(Runnable {
            contractsFacade.getContracts(
                contractIds,
                callback
            )
        })

    override fun getContract(
        contractId: String,
        callback: Callback<ContractStruct>
    ) =
        dispatch(Runnable {
            contractsFacade.getContract(
                contractId,
                callback
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
            MainThreadAccountListener(
                this
            )
        }

    private fun <T> UpdateListener<T>.wrapOriginal(): UpdateListener<T> =
        if (!returnOnMainThread) {
            this
        } else {
            MainThreadUpdateListener(
                this
            )
        }

    private fun dispatch(
        job: Runnable
    ) = dispatcher.dispatch(
        job
    )

}
