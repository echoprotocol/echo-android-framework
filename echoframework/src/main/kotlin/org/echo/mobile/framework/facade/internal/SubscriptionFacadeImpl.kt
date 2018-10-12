package org.echo.mobile.framework.facade.internal

import org.echo.mobile.framework.AccountListener
import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.core.logger.internal.LoggerCoreComponent
import org.echo.mobile.framework.core.socket.SocketCoreComponent
import org.echo.mobile.framework.core.socket.SocketMessengerListener
import org.echo.mobile.framework.exception.LocalException
import org.echo.mobile.framework.facade.SubscriptionFacade
import org.echo.mobile.framework.model.Account
import org.echo.mobile.framework.model.Block
import org.echo.mobile.framework.model.DynamicGlobalProperties
import org.echo.mobile.framework.model.FullAccount
import org.echo.mobile.framework.model.contract.Contract
import org.echo.mobile.framework.model.contract.ContractStruct
import org.echo.mobile.framework.model.network.Network
import org.echo.mobile.framework.service.*
import org.echo.mobile.framework.service.internal.subscription.*
import org.echo.mobile.framework.support.*
import org.echo.mobile.framework.support.concurrent.future.FutureTask
import org.echo.mobile.framework.support.concurrent.future.completeCallback
import org.echo.mobile.framework.support.concurrent.future.wrapResult

/**
 * Implementation of [SubscriptionFacade]
 *
 * @author Dmitriy Bushuev
 */
class SubscriptionFacadeImpl(
    private val socketCoreComponent: SocketCoreComponent,
    private val databaseApiService: DatabaseApiService,
    private val network: Network
) : SubscriptionFacade {

    private val socketMessengerListener by lazy {
        SubscriptionListener()
    }

    @Volatile
    private var subscribed = false

    private val accountSubscriptionManager: AccountSubscriptionManager by lazy {
        AccountSubscriptionManagerImpl(
            network
        )
    }

    private val blockSubscriptionManager: BlockSubscriptionManager by lazy {
        BlockSubscriptionManagerImpl()
    }

    private val blockcnainDataSubscriptionManager: CurrentBlockchainDataSubscriptionManager by lazy {
        CurrentBlockchainDataSubscriptionManagerImpl()
    }

    private val contractSubscriptionManager: ContractSubscriptionManager by lazy {
        ContractSubscriptionManagerImpl()
    }

    override fun subscribeOnAccount(
        nameOrId: String,
        listener: AccountListener,
        callback: Callback<Boolean>
    ) {
        synchronized(this) {
            subscribeGlobal(callback)

            if (!accountSubscriptionManager.registered(nameOrId)) {
                getAccountId(nameOrId)
                    .flatMap { account ->
                        databaseApiService.getFullAccounts(listOf(account), true)
                    }
                    .value { accountsMap ->
                        accountsMap.values.firstOrNull()?.account?.getObjectId()?.let { id ->
                            accountSubscriptionManager.registerListener(id, listener)
                            callback.onSuccess(subscribed)
                        }
                    }
                    .error { error ->
                        LOGGER.log("Account finding error.", error)
                        callback.onError(LocalException(error))
                    }
            } else {
                accountSubscriptionManager.registerListener(nameOrId, listener)
                callback.onSuccess(subscribed)
            }
        }
    }

    private fun subscribeCallBlocking(): Boolean {
        val futureResult = FutureTask<Boolean>()

        databaseApiService.subscribe(false, futureResult.completeCallback())

        var result = false

        futureResult.wrapResult<Exception, Boolean>(false)
            .value {
                result = it
            }
            .error { error ->
                LOGGER.log("Subscription request error", error)
                result = false
            }

        return result
    }

    override fun subscribeOnBlockchainData(
        listener: UpdateListener<DynamicGlobalProperties>,
        callback: Callback<Boolean>
    ) {
        synchronized(this) {
            subscribeGlobal(callback)

            if (!blockcnainDataSubscriptionManager.containListeners()) {
                getCurrentBlockchainData()
                    .value { _ ->
                        blockcnainDataSubscriptionManager.addListener(listener)
                        callback.onSuccess(subscribed)
                    }
                    .error { error ->
                        LOGGER.log("Blockchain data retrieving error.", error)
                        callback.onError(LocalException(error))
                    }
            } else {
                blockcnainDataSubscriptionManager.addListener(listener)
                callback.onSuccess(subscribed)
            }
        }
    }

    override fun subscribeOnBlock(listener: UpdateListener<Block>, callback: Callback<Boolean>) {
        synchronized(this) {
            subscribeGlobal(callback)

            if (!blockSubscriptionManager.containListeners()) {
                getCurrentBlockchainData()
                    .value { _ ->
                        blockSubscriptionManager.addListener(listener)
                        callback.onSuccess(subscribed)
                    }
                    .error { error ->
                        LOGGER.log("Blockchain data retrieving error.", error)
                        callback.onError(LocalException(error))
                    }
            } else {
                blockSubscriptionManager.addListener(listener)
                callback.onSuccess(subscribed)
            }
        }
    }

    override fun subscribeOnContract(
        contractId: String,
        listener: UpdateListener<Contract>,
        callback: Callback<Boolean>
    ) {
        synchronized(this) {
            subscribeGlobal(callback)

            if (!blockSubscriptionManager.containListeners()) {
                getCurrentBlockchainData()
                    .value { _ -> tryInitContractListener(contractId, listener, callback) }
                    .error { error ->
                        LOGGER.log("Blockchain data retrieving error.", error)
                        callback.onError(LocalException(error))
                    }
            } else {
                tryInitContractListener(contractId, listener, callback)
            }
        }
    }

    private fun subscribeGlobal(callback: Callback<Boolean>) {
        if (!subscribed) {
            socketCoreComponent.on(socketMessengerListener)

            this.subscribed = subscribeCallBlocking()
            if (!subscribed) {
                callback.onError(LocalException("Subscription request error"))
            }
        }
    }

    private fun tryInitContractListener(
        contractId: String,
        listener: UpdateListener<Contract>,
        callback: Callback<Boolean>
    ) {
        getContractData(contractId)
            .value { _ ->
                contractSubscriptionManager.registerListener(contractId, listener)
                blockSubscriptionManager
                    .addListener(ContractBlockListenerDelegate(contractSubscriptionManager))
                callback.onSuccess(subscribed)
            }
            .error { error ->
                LOGGER.log("Contract data retrieving error.", error)
                callback.onError(LocalException(error))
            }
    }

    private fun getCurrentBlockchainData(): Result<LocalException, DynamicGlobalProperties> {
        return databaseApiService.getObjects(
            listOf(blockObjectId),
            blockcnainDataSubscriptionManager.mapper
        )
            .flatMap { objects ->
                objects.firstOrNull()?.let {
                    Result.Value(it)
                } ?: Result.Error(LocalException())
            }
            .mapError {
                LocalException("Unable to find required object id for identifier = $blockObjectId")
            }
    }

    private fun getContractData(contractId: String): Result<LocalException, ContractStruct> {
        return databaseApiService.getContract(contractId)
            .mapError {
                LocalException("Unable to find required contract for identifier = $contractId")
            }
    }

    override fun unsubscribeFromAccount(nameOrId: String, callback: Callback<Boolean>) {
        // if there are listeners registered with [nameOrId] - remove them
        // else - request account by [nameOrId] and try remove listeners by account's id
        if (!accountSubscriptionManager.registered(nameOrId)) {
            synchronized(this) {
                if (!accountSubscriptionManager.registered(nameOrId)) {
                    getAccountId(nameOrId)
                        .map { id -> accountSubscriptionManager.removeListeners(id) }
                        .value { existedListeners ->
                            if (existedListeners != null) {
                                callback.onSuccess(true)
                            } else {
                                LOGGER.log("No listeners found for this account")
                                callback.onError(LocalException("No listeners found for this account"))
                            }
                        }
                        .error { error ->
                            LOGGER.log("Account finding error.", error)
                            callback.onError(error)
                        }
                }
            }
        } else {
            accountSubscriptionManager.removeListeners(nameOrId)
            callback.onSuccess(true)
        }
    }

    override fun unsubscribeFromBlock(callback: Callback<Boolean>) {
        if (!blockSubscriptionManager.containListeners()) {
            LOGGER.log("No listeners found for block subscription")
            callback.onError(LocalException("No listeners found for block subscription"))

        } else {
            blockSubscriptionManager.clear()
            callback.onSuccess(true)
        }
    }

    override fun unsubscribeFromBlockchainData(callback: Callback<Boolean>) {
        if (!blockcnainDataSubscriptionManager.containListeners()) {
            LOGGER.log("No listeners found for blockchain changes subscription")
            callback.onError(LocalException("No listeners found for blockchain changes subscription"))

        } else {
            blockcnainDataSubscriptionManager.clear()
            callback.onSuccess(true)
        }
    }

    override fun unsubscribeFromContract(contractId: String, callback: Callback<Boolean>) {
        if (!contractSubscriptionManager.registered(contractId)) {
            LOGGER.log("No listeners found for contract $contractId changes")
            callback.onError(LocalException("No listeners found for contract $contractId changes"))
        } else {
            contractSubscriptionManager.removeListeners(contractId)
            callback.onSuccess(true)
        }
    }

    override fun unsubscribeAll(callback: Callback<Boolean>) {
        synchronized(this) {
            if (subscribed) {
                cancelAllSubscriptions()
                    .value { result ->
                        subscribed = !result

                        socketCoreComponent.off(socketMessengerListener)
                        accountSubscriptionManager.clear()
                        blockSubscriptionManager.clear()
                        blockcnainDataSubscriptionManager.clear()
                        contractSubscriptionManager.clear()
                        callback.onSuccess(result)
                    }
                    .error { error ->
                        callback.onError(error)
                    }
            } else {
                callback.onSuccess(true)
            }
        }
    }

    private fun cancelAllSubscriptions(): Result<LocalException, Boolean> {
        val future = FutureTask<Boolean>()
        databaseApiService.unsubscribe(future.completeCallback())

        return future.wrapResult(false)
    }

    private fun getAccountId(nameOrId: String): Result<LocalException, String> =
        databaseApiService.getFullAccounts(listOf(nameOrId), false)
            .flatMap { accountsMap ->
                accountsMap[nameOrId]?.account?.getObjectId()?.let { Result.Value(it) }
                    ?: Result.Error(LocalException())
            }
            .mapError {
                LocalException("Unable to find required account id for identifier = $nameOrId")
            }


    private fun getAccount(nameOrId: String): Result<LocalException, Account> =
        databaseApiService.getFullAccounts(listOf(nameOrId), false)
            .flatMap { accountsMap ->
                accountsMap[nameOrId]?.account?.let { Result.Value(it) }
                    ?: Result.Error(LocalException())
            }
            .mapError {
                LocalException("Unable to find required account id for identifier = $nameOrId")
            }

    private fun resetState() {
        accountSubscriptionManager.clear()
        blockSubscriptionManager.clear()
        blockcnainDataSubscriptionManager.clear()
        socketCoreComponent.off(socketMessengerListener)
    }

    private inner class SubscriptionListener : SocketMessengerListener {

        override fun onEvent(event: String) {
            // no need to process other events
            if (event.toJsonObject()?.get(METHOD_KEY)?.asString != NOTICE_METHOD_KEY) {
                return
            }

            processBlockchainData(event)?.let { blockchainData ->
                if (blockSubscriptionManager.containListeners()) {
                    processBlockData(blockchainData)
                }

            } ?: let {
                if (accountSubscriptionManager.containsListeners()) {
                    processAccountData(event)
                }
            }
        }

        private fun processBlockchainData(event: String): DynamicGlobalProperties? {
            val blockchainData =
                blockcnainDataSubscriptionManager.processEvent(event) ?: return null

            blockcnainDataSubscriptionManager.notify(blockchainData)

            return blockchainData
        }

        private fun processBlockData(blockchainData: DynamicGlobalProperties) {
            val blockNumber = blockchainData.headBlockNumber.toString()
            databaseApiService.getBlock(blockNumber, BlockSubscriptionCallback())
        }

        private fun processAccountData(event: String) {
            val accountIds = accountSubscriptionManager.processEvent(event)
            databaseApiService.getFullAccounts(
                accountIds,
                false,
                FullAccountSubscriptionCallback(accountIds)
            )
        }

        override fun onFailure(error: Throwable) = resetState()

        override fun onConnected() {
        }

        override fun onDisconnected() = resetState()

        private inner class FullAccountSubscriptionCallback(private val accountIds: List<String>) :
            Callback<Map<String, FullAccount>> {
            override fun onSuccess(result: Map<String, FullAccount>) {
                accountIds.forEach { accountId ->
                    val account = result[accountId] ?: return

                    accountSubscriptionManager.notify(account)
                }
            }

            override fun onError(error: LocalException) {
            }

        }

        private inner class BlockSubscriptionCallback : Callback<Block> {
            override fun onSuccess(result: Block) {
                blockSubscriptionManager.notify(result)
            }

            override fun onError(error: LocalException) {
            }

        }
    }

    companion object {
        const val METHOD_KEY = "method"
        private const val NOTICE_METHOD_KEY = "notice"

        const val blockObjectId =
            CurrentBlockchainDataSubscriptionManager.CURRENT_BLOCKCHAIN_DATA_OBJECT_ID

        private val LOGGER = LoggerCoreComponent.create(SubscriptionFacadeImpl::class.java.name)
    }

}
