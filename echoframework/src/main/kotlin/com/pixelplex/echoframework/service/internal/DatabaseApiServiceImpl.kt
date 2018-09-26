package com.pixelplex.echoframework.service.internal

import com.pixelplex.echoframework.Callback
import com.pixelplex.echoframework.ILLEGAL_ID
import com.pixelplex.echoframework.core.socket.SocketCoreComponent
import com.pixelplex.echoframework.exception.LocalException
import com.pixelplex.echoframework.model.*
import com.pixelplex.echoframework.model.contract.ContractInfo
import com.pixelplex.echoframework.model.contract.ContractResult
import com.pixelplex.echoframework.model.contract.ContractStruct
import com.pixelplex.echoframework.model.network.Network
import com.pixelplex.echoframework.model.socketoperations.*
import com.pixelplex.echoframework.service.DatabaseApiService
import com.pixelplex.echoframework.support.Result
import com.pixelplex.echoframework.support.concurrent.future.FutureTask
import com.pixelplex.echoframework.support.concurrent.future.completeCallback
import com.pixelplex.echoframework.support.concurrent.future.wrapResult
import com.pixelplex.echoframework.support.dematerialize
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
            callback = object : Callback<Map<String, FullAccount>> {

                override fun onSuccess(result: Map<String, FullAccount>) {
                    fillAccounts(result, callback)
                }

                override fun onError(error: LocalException) {
                    callback.onError(error)
                }

            },
            network = network
        )
        socketCoreComponent.emit(fullAccountsOperation)
    }

    override fun getFullAccounts(
        namesOrIds: List<String>,
        subscribe: Boolean
    ): Result<LocalException, Map<String, FullAccount>> {
        val future = FutureTask<Map<String, FullAccount>>()
        getFullAccounts(namesOrIds, subscribe, future.completeCallback())
        return future.wrapResult(mapOf())
    }

    private fun fillAccounts(
        accounts: Map<String, FullAccount>,
        callback: Callback<Map<String, FullAccount>>
    ) {
        val requiredAssets = getRequiredAssets(accounts.values)
        getAssets(requiredAssets.toList(), object : Callback<List<Asset>> {

            override fun onSuccess(result: List<Asset>) {
                fillAssets(accounts, result, callback)
            }

            override fun onError(error: LocalException) {
                callback.onError(error)
            }

        })
    }

    private fun getRequiredAssets(accounts: Collection<FullAccount>): List<String> {
        val requiredAssets = mutableSetOf<String>()

        accounts.forEach { fullAccount ->
            val balanceAssets =
                fullAccount.balances?.map { it.asset!!.getObjectId() } ?: emptyList()
            val accountAssets = fullAccount.assets?.map { it.getObjectId() } ?: emptyList()

            requiredAssets.addAll(balanceAssets)
            requiredAssets.addAll(accountAssets)
        }

        return requiredAssets.toList()
    }

    private fun fillAssets(
        accounts: Map<String, FullAccount>,
        assets: List<Asset>,
        callback: Callback<Map<String, FullAccount>>
    ) {
        accounts.values.forEach { fullAccount ->
            fullAccount.balances?.forEach { balance ->
                balance.asset =
                        assets.find { asset -> asset.getObjectId() == balance.asset?.getObjectId() } ?:
                        balance.asset
            }

            val filledAssets = mutableListOf<Asset>()
            fullAccount.assets?.forEach { asset ->
                val candidate =
                    assets.find { it.getObjectId() == asset.getObjectId() } ?: asset
                filledAssets.add(candidate)
            }

            fullAccount.assets = filledAssets
        }

        callback.onSuccess(accounts)
    }

    override fun getChainId(): Result<Exception, String> {
        val future = FutureTask<String>()
        val chainIdOperation = GetChainIdSocketOperation(
            id,
            callId = socketCoreComponent.currentId,
            callback = future.completeCallback()
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
            callback = future.completeCallback()
        )
        socketCoreComponent.emit(blockDataOperation)

        return future.wrapResult()
    }

    override fun getBlock(blockNumber: String): Result<LocalException, Block> {
        val blockFuture = FutureTask<Block>()

        getBlock(blockNumber, blockFuture.completeCallback())

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
            callback = future.completeCallback()
        )
        socketCoreComponent.emit(requiredFeesOperation)

        return future.wrapResult()
    }

    override fun listAssets(lowerBound: String, limit: Int, callback: Callback<List<Asset>>) {
        val operation = ListAssetsSocketOperation(id, lowerBound, limit, callback = callback)

        socketCoreComponent.emit(operation)
    }

    override fun getAssets(assetIds: List<String>, callback: Callback<List<Asset>>) {
        val operation = GetAssetsSocketOperation(
            id,
            assetIds.toTypedArray(),
            socketCoreComponent.currentId,
            callback
        )

        socketCoreComponent.emit(operation)
    }

    private fun getAssets(assetIds: List<String>): Result<LocalException, List<Asset>> {
        val futureAssets = FutureTask<List<Asset>>()
        getAssets(assetIds, futureAssets.completeCallback())

        return futureAssets.wrapResult()
    }

    override fun callContractNoChangingState(
        contractId: String,
        registrarNameOrId: String,
        assetId: String,
        byteCode: String
    ): Result<LocalException, String> {
        val future = FutureTask<String>()
        val operation = QueryContractSocketOperation(
            id,
            contractId,
            registrarNameOrId,
            assetId,
            byteCode,
            callId = socketCoreComponent.currentId,
            callback = future.completeCallback()
        )
        socketCoreComponent.emit(operation)

        return future.wrapResult()
    }

    override fun getContractResult(historyId: String): Result<LocalException, ContractResult> {
        val future = FutureTask<ContractResult>()
        val operation = GetContractResultSocketOperation(
            id,
            historyId,
            callId = socketCoreComponent.currentId,
            callback = future.completeCallback()
        )
        socketCoreComponent.emit(operation)

        return future.wrapResult()
    }

    override fun getAllContracts(): Result<LocalException, List<ContractInfo>> {
        val future = FutureTask<List<ContractInfo>>()
        val operation = GetAllContractsSocketOperation(
            id,
            callId = socketCoreComponent.currentId,
            callback = future.completeCallback()
        )
        socketCoreComponent.emit(operation)

        return future.wrapResult()
    }

    override fun getContracts(contractIds: List<String>): Result<LocalException, List<ContractInfo>> {
        val future = FutureTask<List<ContractInfo>>()
        val operation = GetContractsSocketOperation(
            id,
            contractIds,
            callId = socketCoreComponent.currentId,
            callback = future.completeCallback()
        )
        socketCoreComponent.emit(operation)

        return future.wrapResult()
    }

    override fun getContract(contractId: String): Result<LocalException, ContractStruct> {
        val future = FutureTask<ContractStruct>()
        val operation = GetContractSocketOperation(
            id,
            contractId,
            callId = socketCoreComponent.currentId,
            callback = future.completeCallback()
        )
        socketCoreComponent.emit(operation)

        return future.wrapResult()
    }

    override fun subscribe(clearFilter: Boolean, callback: Callback<Boolean>) {
        socketCoreComponent.emit(createSubscriptionOperation(clearFilter, callback))
    }

    private fun createSubscriptionOperation(clearFilter: Boolean, callback: Callback<Boolean>) =
        SetSubscribeCallbackSocketOperation(
            id,
            clearFilter,
            socketCoreComponent.currentId,
            callback
        )

    override fun unsubscribe(callback: Callback<Boolean>) {
        val cancelSubscriptionsOperation = CancelAllSubscriptionsSocketOperation(
            id,
            callId = socketCoreComponent.currentId,
            callback = callback
        )

        socketCoreComponent.emit(cancelSubscriptionsOperation)
    }

}
