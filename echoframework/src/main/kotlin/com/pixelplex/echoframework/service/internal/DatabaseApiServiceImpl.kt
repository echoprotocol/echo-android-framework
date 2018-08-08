package com.pixelplex.echoframework.service.internal

import com.pixelplex.echoframework.AccountListener
import com.pixelplex.echoframework.Callback
import com.pixelplex.echoframework.ILLEGAL_ID
import com.pixelplex.echoframework.core.logger.internal.LoggerCoreComponent
import com.pixelplex.echoframework.core.socket.SocketCoreComponent
import com.pixelplex.echoframework.core.socket.SocketMessengerListener
import com.pixelplex.echoframework.exception.LocalException
import com.pixelplex.echoframework.model.*
import com.pixelplex.echoframework.model.contract.ContractInfo
import com.pixelplex.echoframework.model.contract.ContractResult
import com.pixelplex.echoframework.model.contract.ContractStruct
import com.pixelplex.echoframework.model.network.Network
import com.pixelplex.echoframework.model.socketoperations.*
import com.pixelplex.echoframework.service.DatabaseApiService
import com.pixelplex.echoframework.support.*
import com.pixelplex.echoframework.support.concurrent.future.FutureTask
import com.pixelplex.echoframework.support.concurrent.future.wrapResult
import java.util.concurrent.TimeUnit

/**
 * Implementation of [DatabaseApiService]
 *
 * Encapsulates logic of preparing API calls to [SocketCoreComponent]
 *
 * @author Dmitriy Bushuev
 */
class DatabaseApiServiceImpl(
    private val socketCoreComponent: SocketCoreComponent,
    private val network: Network
) : DatabaseApiService {

    override var id: Int = ILLEGAL_ID

    private var socketMessengerListener = SubscriptionListener()

    @Volatile
    private var subscribed = false

    private val subscriptionManager: AccountSubscriptionManager by lazy {
        AccountSubscriptionManagerImpl(network)
    }

    override fun getFullAccounts(
        namesOrIds: List<String>,
        subscribe: Boolean,
        callback: Callback<Map<String, FullAccount>>
    ) {
        val fullAccountsOperation = FullAccountsSocketOperation(
            id,
            namesOrIds,
            subscribe,
            callId = socketCoreComponent.currentId,
            callback = callback,
            network = network
        )
        socketCoreComponent.emit(fullAccountsOperation)
    }

    override fun getFullAccounts(
        namesOrIds: List<String>,
        subscribe: Boolean
    ): Result<Exception, Map<String, FullAccount>> {

        val future = FutureTask<Map<String, FullAccount>>()
        val fullAccountsOperation = FullAccountsSocketOperation(
            id,
            namesOrIds,
            subscribe,
            callId = socketCoreComponent.currentId,
            callback = object : Callback<Map<String, FullAccount>> {
                override fun onSuccess(result: Map<String, FullAccount>) {
                    future.setComplete(result)
                }

                override fun onError(error: LocalException) {
                    future.setComplete(error)
                }
            },
            network = network
        )
        socketCoreComponent.emit(fullAccountsOperation)

        return future.wrapResult(mapOf())
    }

    override fun getChainId(): Result<Exception, String> {
        val future = FutureTask<String>()
        val chainIdOperation = GetChainIdSocketOperation(
            id,
            callId = socketCoreComponent.currentId,
            callback = object : Callback<String> {
                override fun onSuccess(result: String) {
                    future.setComplete(result)
                }

                override fun onError(error: LocalException) {
                    future.setComplete(error)
                }
            }
        )
        socketCoreComponent.emit(chainIdOperation)

        return future.wrapResult()
    }

    override fun getBlockData(): BlockData {
        val dynamicProperties = getDynamicGlobalProperties().dematerialize()
        val expirationTime = TimeUnit.MILLISECONDS.toSeconds(dynamicProperties.date!!.time) +
                Transaction.DEFAULT_EXPIRATION_TIME
        val headBlockId = dynamicProperties.headBlockId
        val headBlockNumber = dynamicProperties.headBlockNumber
        return BlockData(headBlockNumber, headBlockId, expirationTime)
    }

    override fun getDynamicGlobalProperties(): Result<Exception, DynamicGlobalProperties> {
        val future = FutureTask<DynamicGlobalProperties>()
        val blockDataOperation = BlockDataSocketOperation(
            id,
            socketCoreComponent.currentId,
            callback = object : Callback<DynamicGlobalProperties> {
                override fun onSuccess(result: DynamicGlobalProperties) {
                    future.setComplete(result)
                }

                override fun onError(error: LocalException) {
                    future.setComplete(error)
                }
            }
        )
        socketCoreComponent.emit(blockDataOperation)

        return future.wrapResult()
    }

    override fun getBlock(blockNumber: String): Result<LocalException, Block> {
        val blockFuture = FutureTask<Block>()

        getBlock(blockNumber, object : Callback<Block> {

            override fun onSuccess(result: Block) {
                blockFuture.setComplete(result)
            }

            override fun onError(error: LocalException) {
                blockFuture.setComplete(error)
            }

        })

        return blockFuture.wrapResult()
    }

    override fun getBlock(blockNumber: String, callback: Callback<Block>) {
        val blockOperation =
            GetBlockSocketOperation(
                id,
                blockNumber,
                callId = socketCoreComponent.currentId,
                callback = callback,
                network = network
            )

        socketCoreComponent.emit(blockOperation)
    }

    override fun getRequiredFees(
        operations: List<BaseOperation>,
        asset: Asset
    ): Result<Exception, List<AssetAmount>> {
        val future = FutureTask<List<AssetAmount>>()
        val requiredFeesOperation = RequiredFeesSocketOperation(
            id,
            operations,
            asset,
            callId = socketCoreComponent.currentId,
            callback = object : Callback<List<AssetAmount>> {
                override fun onSuccess(result: List<AssetAmount>) {
                    future.setComplete(result)
                }

                override fun onError(error: LocalException) {
                    future.setComplete(error)
                }
            }
        )
        socketCoreComponent.emit(requiredFeesOperation)

        return future.wrapResult()
    }

    override fun subscribeOnAccount(
        nameOrId: String,
        listener: AccountListener
    ) {
        synchronized(this) {
            if (!subscribed) {
                socketCoreComponent.on(socketMessengerListener)

                this.subscribed = subscribeCallBlocking()
            }

            if (!subscriptionManager.registered(nameOrId)) {
                getFullAccounts(listOf(nameOrId), true)
                    .value { accountsMap ->
                        accountsMap[nameOrId]?.account?.getObjectId()?.let { requiredAccountId ->
                            subscriptionManager.registerListener(requiredAccountId, listener)
                        }
                    }
            } else {
                subscriptionManager.registerListener(nameOrId, listener)
            }
        }
    }

    override fun unsubscribeFromAccount(nameOrId: String, callback: Callback<Boolean>) {
        // if there are listeners registered with [nameOrId] - remove them
        // else - request account by [nameOrId] and try remove listeners by account's id
        if (!subscriptionManager.registered(nameOrId)) {
            synchronized(this) {
                if (!subscriptionManager.registered(nameOrId)) {
                    getAccountId(nameOrId)
                        .map { id -> subscriptionManager.removeListeners(id) }
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
            subscriptionManager.removeListeners(nameOrId)
        }
    }

    private fun getAccountId(nameOrId: String): Result<LocalException, String> =
        getFullAccounts(listOf(nameOrId), false)
            .flatMap { accountsMap ->
                accountsMap[nameOrId]?.account?.getObjectId()?.let { Result.Value(it) }
                    ?: Result.Error(LocalException())
            }
            .mapError {
                LocalException("Unable to find required account id for identifier = $nameOrId")
            }

    override fun unsubscribeAll(callback: Callback<Boolean>) =
        synchronized(this) {
            subscriptionManager.clear()
        }

    override fun callContractNoChangingState(
        contractId: String,
        registrarNameOrId: String,
        assetId: String,
        byteCode: String
    ): Result<LocalException, String> {
        val future = FutureTask<String>()
        val requiredFeesOperation = QueryContractSocketOperation(
            id,
            contractId,
            registrarNameOrId,
            assetId,
            byteCode,
            callId = socketCoreComponent.currentId,
            callback = object : Callback<String> {
                override fun onSuccess(result: String) {
                    future.setComplete(result)
                }

                override fun onError(error: LocalException) {
                    future.setComplete(error)
                }
            }
        )
        socketCoreComponent.emit(requiredFeesOperation)

        return future.wrapResult()
    }

    override fun getContractResult(historyId: String): Result<LocalException, ContractResult> {
        val future = FutureTask<ContractResult>()
        val requiredFeesOperation = GetContractResultSocketOperation(
            id,
            historyId,
            callId = socketCoreComponent.currentId,
            callback = object : Callback<ContractResult> {
                override fun onSuccess(result: ContractResult) {
                    future.setComplete(result)
                }

                override fun onError(error: LocalException) {
                    future.setComplete(error)
                }
            }
        )
        socketCoreComponent.emit(requiredFeesOperation)

        return future.wrapResult()
    }

    override fun getAllContracts(): Result<LocalException, List<ContractInfo>> {
        val future = FutureTask<List<ContractInfo>>()
        val requiredFeesOperation = GetAllContractsSocketOperation(
            id,
            callId = socketCoreComponent.currentId,
            callback = object : Callback<List<ContractInfo>> {
                override fun onSuccess(result: List<ContractInfo>) {
                    future.setComplete(result)
                }

                override fun onError(error: LocalException) {
                    future.setComplete(error)
                }
            }
        )
        socketCoreComponent.emit(requiredFeesOperation)

        return future.wrapResult()
    }

    override fun getContracts(contractIds: List<String>): Result<LocalException, List<ContractInfo>> {
        val future = FutureTask<List<ContractInfo>>()
        val requiredFeesOperation = GetContractsSocketOperation(
            id,
            contractIds,
            callId = socketCoreComponent.currentId,
            callback = object : Callback<List<ContractInfo>> {
                override fun onSuccess(result: List<ContractInfo>) {
                    future.setComplete(result)
                }

                override fun onError(error: LocalException) {
                    future.setComplete(error)
                }
            }
        )
        socketCoreComponent.emit(requiredFeesOperation)

        return future.wrapResult()
    }

    override fun getContract(contractId: String): Result<LocalException, ContractStruct> {
        val future = FutureTask<ContractStruct>()
        val requiredFeesOperation = GetContractSocketOperation(
            id,
            contractId,
            callId = socketCoreComponent.currentId,
            callback = object : Callback<ContractStruct> {
                override fun onSuccess(result: ContractStruct) {
                    future.setComplete(result)
                }

                override fun onError(error: LocalException) {
                    future.setComplete(error)
                }
            }
        )
        socketCoreComponent.emit(requiredFeesOperation)

        return future.wrapResult()
    }

    private fun subscribeCallBlocking(): Boolean {
        val futureResult = FutureTask<Boolean>()

        val subscriptionOperation = createSubscriptionOperation(true, object : Callback<Any> {
            override fun onSuccess(result: Any) {
                futureResult.setComplete(true)
            }

            override fun onError(error: LocalException) {
                futureResult.setComplete(error)
            }

        })

        socketCoreComponent.emit(subscriptionOperation)

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

    private fun createSubscriptionOperation(clearFilter: Boolean, callback: Callback<Any>) =
        SetSubscribeCallbackSocketOperation(
            id,
            clearFilter,
            socketCoreComponent.currentId,
            callback
        )

    private inner class SubscriptionListener : SocketMessengerListener {

        override fun onEvent(event: String) {
            // no need to process other events)
            if (event.toJsonObject()?.get(DatabaseApiServiceImpl.METHOD_KEY)?.asString !=
                DatabaseApiServiceImpl.NOTICE_METHOD_KEY
            ) {
                return
            }

            subscriptionManager.processEvent(event)
        }

        // implement failures
        override fun onFailure(error: Throwable) {
        }

        override fun onConnected() {
        }

        override fun onDisconnected() {
        }

    }

    companion object {
        const val METHOD_KEY = "method"
        private const val NOTICE_METHOD_KEY = "notice"

        private val LOGGER = LoggerCoreComponent.create(DatabaseApiServiceImpl::class.java.name)
    }

}


