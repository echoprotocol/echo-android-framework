package org.echo.mobile.framework

import org.echo.mobile.framework.core.crypto.internal.eddsa.EdDSASecurityProvider
import org.echo.mobile.framework.core.logger.internal.LoggerCoreComponent
import org.echo.mobile.framework.core.mapper.internal.MapperCoreComponentImpl
import org.echo.mobile.framework.core.socket.internal.SocketCoreComponentImpl
import org.echo.mobile.framework.facade.AssetsFacade
import org.echo.mobile.framework.facade.AuthenticationFacade
import org.echo.mobile.framework.facade.BitcoinSidechainFacade
import org.echo.mobile.framework.facade.CommonSidechainFacade
import org.echo.mobile.framework.facade.ContractsFacade
import org.echo.mobile.framework.facade.ERC20SidechainFacade
import org.echo.mobile.framework.facade.EthereumSidechainFacade
import org.echo.mobile.framework.facade.FeeFacade
import org.echo.mobile.framework.facade.InformationFacade
import org.echo.mobile.framework.facade.InitializerFacade
import org.echo.mobile.framework.facade.SubscriptionFacade
import org.echo.mobile.framework.facade.TransactionsFacade
import org.echo.mobile.framework.facade.internal.AssetsFacadeImpl
import org.echo.mobile.framework.facade.internal.AuthenticationFacadeImpl
import org.echo.mobile.framework.facade.internal.BitcoinSidechainFacadeImpl
import org.echo.mobile.framework.facade.internal.CommonSidechainFacadeImpl
import org.echo.mobile.framework.facade.internal.ContractsFacadeImpl
import org.echo.mobile.framework.facade.internal.ERC20SidechainFacadeImpl
import org.echo.mobile.framework.facade.internal.EthereumSidechainFacadeImpl
import org.echo.mobile.framework.facade.internal.FeeFacadeImpl
import org.echo.mobile.framework.facade.internal.InformationFacadeImpl
import org.echo.mobile.framework.facade.internal.InitializerFacadeImpl
import org.echo.mobile.framework.facade.internal.NotificationsHelper
import org.echo.mobile.framework.facade.internal.SubscriptionFacadeImpl
import org.echo.mobile.framework.facade.internal.TransactionsFacadeImpl
import org.echo.mobile.framework.model.Asset
import org.echo.mobile.framework.model.AssetAmount
import org.echo.mobile.framework.model.Balance
import org.echo.mobile.framework.model.Block
import org.echo.mobile.framework.model.BtcAddress
import org.echo.mobile.framework.model.Deposit
import org.echo.mobile.framework.model.DynamicGlobalProperties
import org.echo.mobile.framework.model.ERC20Deposit
import org.echo.mobile.framework.model.ERC20Token
import org.echo.mobile.framework.model.ERC20Withdrawal
import org.echo.mobile.framework.model.EthAddress
import org.echo.mobile.framework.model.FullAccount
import org.echo.mobile.framework.model.GlobalProperties
import org.echo.mobile.framework.model.HistoryResponse
import org.echo.mobile.framework.model.SidechainType
import org.echo.mobile.framework.model.TransactionResult
import org.echo.mobile.framework.model.Withdraw
import org.echo.mobile.framework.model.contract.ContractBalance
import org.echo.mobile.framework.model.contract.ContractFee
import org.echo.mobile.framework.model.contract.ContractInfo
import org.echo.mobile.framework.model.contract.ContractLog
import org.echo.mobile.framework.model.contract.ContractResult
import org.echo.mobile.framework.model.contract.ContractStruct
import org.echo.mobile.framework.model.contract.input.InputValue
import org.echo.mobile.framework.service.AccountHistoryApiService
import org.echo.mobile.framework.service.CryptoApiService
import org.echo.mobile.framework.service.DatabaseApiService
import org.echo.mobile.framework.service.LoginApiService
import org.echo.mobile.framework.service.NetworkBroadcastApiService
import org.echo.mobile.framework.service.RegistrationApiService
import org.echo.mobile.framework.service.UpdateListener
import org.echo.mobile.framework.service.internal.AccountHistoryApiServiceImpl
import org.echo.mobile.framework.service.internal.CryptoApiServiceImpl
import org.echo.mobile.framework.service.internal.DatabaseApiServiceImpl
import org.echo.mobile.framework.service.internal.LoginApiServiceImpl
import org.echo.mobile.framework.service.internal.NetworkBroadcastApiServiceImpl
import org.echo.mobile.framework.service.internal.RegistrationApiServiceImpl
import org.echo.mobile.framework.service.internal.subscription.ContractLogsSubscriptionManagerImpl
import org.echo.mobile.framework.service.internal.subscription.RegistrationSubscriptionManagerImpl
import org.echo.mobile.framework.service.internal.subscription.TransactionSubscriptionManagerImpl
import org.echo.mobile.framework.support.FeeRatioProvider
import org.echo.mobile.framework.support.Settings
import org.echo.mobile.framework.support.concurrent.Dispatcher
import org.echo.mobile.framework.support.concurrent.ExecutorServiceDispatcher
import org.echo.mobile.framework.support.concurrent.MainThreadAccountListener
import org.echo.mobile.framework.support.concurrent.MainThreadCallback
import org.echo.mobile.framework.support.concurrent.MainThreadUpdateListener
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
    private val ethereumSidechainFacade: EthereumSidechainFacade
    private val bitcoinSidechainFacade: BitcoinSidechainFacade
    private val commonSidechainFacade: CommonSidechainFacade
    private val erc20SidechainFacade: ERC20SidechainFacade

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
            registrationNotificationsHelper,
            settings.transactionExpirationDelaySeconds
        )

        val feeRatioProvider = FeeRatioProvider(settings.feeRatio)

        feeFacade = FeeFacadeImpl(
            databaseApiService,
            settings.cryptoComponent,
            feeRatioProvider,
            settings.transactionExpirationDelaySeconds
        )
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
            settings.cryptoComponent,
            settings.transactionExpirationDelaySeconds
        )

        val transactionSubscriptionManager = TransactionSubscriptionManagerImpl(settings.network)

        val notifiedTransactionsHelper =
            NotificationsHelper(socketCoreComponent, transactionSubscriptionManager)

        val contractLogsSubscriptionManager = ContractLogsSubscriptionManagerImpl()

        val notifiedContractLogsHelper =
            NotificationsHelper(socketCoreComponent, contractLogsSubscriptionManager)

        assetsFacade = AssetsFacadeImpl(
            databaseApiService,
            networkBroadcastApiService,
            settings.cryptoComponent,
            notifiedTransactionsHelper,
            settings.transactionExpirationDelaySeconds
        )
        contractsFacade = ContractsFacadeImpl(
            databaseApiService,
            networkBroadcastApiService,
            settings.cryptoComponent,
            notifiedTransactionsHelper,
            notifiedContractLogsHelper,
            feeRatioProvider,
            settings.transactionExpirationDelaySeconds
        )

        val notifiedEthAddressHelper =
            NotificationsHelper(socketCoreComponent, transactionSubscriptionManager)
        ethereumSidechainFacade = EthereumSidechainFacadeImpl(
            databaseApiService,
            networkBroadcastApiService,
            settings.cryptoComponent,
            notifiedEthAddressHelper,
            settings.transactionExpirationDelaySeconds
        )
        bitcoinSidechainFacade = BitcoinSidechainFacadeImpl(
            databaseApiService,
            networkBroadcastApiService,
            settings.cryptoComponent,
            notifiedEthAddressHelper,
            settings.transactionExpirationDelaySeconds
        )
        commonSidechainFacade = CommonSidechainFacadeImpl(databaseApiService)
        erc20SidechainFacade = ERC20SidechainFacadeImpl(
            databaseApiService,
            networkBroadcastApiService,
            settings.cryptoComponent,
            notifiedEthAddressHelper,
            settings.transactionExpirationDelaySeconds
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

    override fun register(
        userName: String,
        wif: String,
        evmAddress: String?,
        callback: Callback<Boolean>
    ) =
        dispatch(Runnable {
            authenticationFacade.register(
                userName,
                wif,
                evmAddress,
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

    override fun getFeeForContractCreateOperation(
        userNameOrId: String,
        amount: String,
        byteCode: String,
        assetId: String,
        feeAsset: String?,
        callback: Callback<AssetAmount>
    ) = dispatch(Runnable {
        feeFacade.getFeeForContractCreateOperation(
            userNameOrId,
            amount,
            byteCode,
            assetId,
            feeAsset,
            callback.wrapOriginal()
        )
    })

    override fun getFeeForWithdrawErc20Operation(
        accountNameOrId: String,
        ethAddress: String,
        ethTokenId: String,
        value: String,
        feeAsset: String,
        callback: Callback<AssetAmount>
    ) = dispatch(Runnable {
        feeFacade.getFeeForWithdrawErc20Operation(
            accountNameOrId,
            ethAddress,
            ethTokenId,
            value,
            feeAsset,
            callback.wrapOriginal()
        )
    })

    override fun getFeeForWithdrawEthereumOperation(
        accountNameOrId: String,
        ethAddress: String,
        value: String,
        feeAsset: String,
        callback: Callback<AssetAmount>
    ) = dispatch(Runnable {
        feeFacade.getFeeForWithdrawEthereumOperation(
            accountNameOrId,
            ethAddress,
            value,
            feeAsset,
            callback.wrapOriginal()
        )
    })

    override fun getFeeForWithdrawBtcOperation(
        accountNameOrId: String,
        btcAddress: String,
        value: String,
        feeAsset: String,
        callback: Callback<AssetAmount>
    ) = dispatch(Runnable {
        feeFacade.getFeeForWithdrawBtcOperation(
            accountNameOrId,
            btcAddress,
            value,
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
        amount: String,
        contractId: String,
        methodName: String,
        methodParams: List<InputValue>,
        callback: Callback<String>
    ) = dispatch(Runnable {
        contractsFacade.queryContract(
            userNameOrId,
            assetId,
            amount,
            contractId,
            methodName,
            methodParams,
            callback
        )
    })

    override fun queryContract(
        userNameOrId: String,
        assetId: String,
        amount: String,
        contractId: String,
        code: String,
        callback: Callback<String>
    ) = dispatch(Runnable {
        contractsFacade.queryContract(
            userNameOrId,
            assetId,
            amount,
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
        broadcastCallback: Callback<Boolean>,
        resultCallback: Callback<List<ContractLog>>?
    ) = dispatch(Runnable {
        contractsFacade.getContractLogs(
            contractId, fromBlock, toBlock, broadcastCallback, resultCallback
        )
    })

    override fun generateEthereumAddress(
        accountNameOrId: String,
        wif: String,
        broadcastCallback: Callback<Boolean>,
        resultCallback: Callback<TransactionResult>?
    ) =
        dispatch(Runnable {
            ethereumSidechainFacade.generateEthereumAddress(
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
            ethereumSidechainFacade.ethWithdraw(
                accountNameOrId,
                wif,
                ethAddress,
                value,
                feeAsset,
                broadcastCallback,
                resultCallback
            )
        })

    override fun btcWithdraw(
        accountNameOrId: String,
        wif: String,
        btcAddress: String,
        value: String,
        feeAsset: String,
        broadcastCallback: Callback<Boolean>,
        resultCallback: Callback<TransactionResult>?
    ) =
        dispatch(Runnable {
            bitcoinSidechainFacade.btcWithdraw(
                accountNameOrId,
                wif,
                btcAddress,
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
            ethereumSidechainFacade.getEthereumAddress(accountNameOrId, callback)
        })

    override fun getAccountDeposits(
        accountNameOrId: String,
        sidechainType: SidechainType?,
        callback: Callback<List<Deposit?>>
    ) =
        dispatch(Runnable {
            commonSidechainFacade.getAccountDeposits(accountNameOrId, sidechainType, callback)
        })

    override fun getAccountWithdrawals(
        accountNameOrId: String,
        sidechainType: SidechainType?,
        callback: Callback<List<Withdraw?>>
    ) =
        dispatch(Runnable {
            commonSidechainFacade.getAccountWithdrawals(accountNameOrId, sidechainType, callback)
        })

    override fun generateBitcoinAddress(
        accountNameOrId: String,
        wif: String,
        backupAddress: String,
        broadcastCallback: Callback<Boolean>,
        resultCallback: Callback<TransactionResult>?
    ) =
        dispatch(Runnable {
            bitcoinSidechainFacade.generateBitcoinAddress(
                accountNameOrId, wif, backupAddress, broadcastCallback, resultCallback
            )
        })

    override fun getBitcoinAddress(accountNameOrId: String, callback: Callback<BtcAddress>) =
        dispatch(Runnable {
            bitcoinSidechainFacade.getBitcoinAddress(accountNameOrId, callback)
        })

    override fun registerERC20Token(
        accountNameOrId: String,
        wif: String,
        ethAddress: String,
        name: String,
        symbol: String,
        decimals: String,
        feeAsset: String,
        broadcastCallback: Callback<Boolean>,
        resultCallback: Callback<TransactionResult>?
    ) =
        dispatch(Runnable {
            erc20SidechainFacade.registerERC20Token(
                accountNameOrId,
                wif,
                ethAddress,
                name,
                symbol,
                decimals,
                feeAsset,
                broadcastCallback,
                resultCallback
            )
        })

    override fun getERC20TokenByAddress(address: String, callback: Callback<ERC20Token>) =
        dispatch(Runnable {
            erc20SidechainFacade.getERC20TokenByAddress(
                address, callback
            )
        })

    override fun getERC20TokenByTokenId(tokenId: String, callback: Callback<ERC20Token>) =
        dispatch(Runnable {
            erc20SidechainFacade.getERC20TokenByTokenId(
                tokenId, callback
            )
        })

    override fun checkERC20Token(contractId: String, callback: Callback<Boolean>) =
        dispatch(Runnable {
            erc20SidechainFacade.checkERC20Token(
                contractId, callback
            )
        })

    override fun getERC20AccountDeposits(
        accountNameOrId: String,
        callback: Callback<List<ERC20Deposit>>
    ) =
        dispatch(Runnable {
            erc20SidechainFacade.getERC20AccountDeposits(
                accountNameOrId, callback
            )
        })

    override fun getERC20AccountWithdrawals(
        accountNameOrId: String,
        callback: Callback<List<ERC20Withdrawal>>
    ) =
        dispatch(Runnable {
            erc20SidechainFacade.getERC20AccountWithdrawals(
                accountNameOrId, callback
            )
        })

    override fun erc20Withdraw(
        accountNameOrId: String,
        wif: String,
        ethAddress: String,
        ethTokenId: String,
        value: String,
        feeAsset: String,
        broadcastCallback: Callback<Boolean>,
        resultCallback: Callback<TransactionResult>?
    ) =
        dispatch(Runnable {
            erc20SidechainFacade.erc20Withdraw(
                accountNameOrId,
                wif,
                ethAddress,
                ethTokenId,
                value,
                feeAsset,
                broadcastCallback,
                resultCallback
            )
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
