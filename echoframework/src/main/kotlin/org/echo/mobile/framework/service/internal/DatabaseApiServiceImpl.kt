package org.echo.mobile.framework.service.internal

import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.ILLEGAL_ID
import org.echo.mobile.framework.core.crypto.CryptoCoreComponent
import org.echo.mobile.framework.core.mapper.ObjectMapper
import org.echo.mobile.framework.core.socket.SocketCoreComponent
import org.echo.mobile.framework.exception.LocalException
import org.echo.mobile.framework.model.Asset
import org.echo.mobile.framework.model.AssetAmount
import org.echo.mobile.framework.model.BaseOperation
import org.echo.mobile.framework.model.Block
import org.echo.mobile.framework.model.BlockData
import org.echo.mobile.framework.model.DynamicGlobalProperties
import org.echo.mobile.framework.model.FullAccount
import org.echo.mobile.framework.model.GlobalProperties
import org.echo.mobile.framework.model.GrapheneObject
import org.echo.mobile.framework.model.Log
import org.echo.mobile.framework.model.SidechainTransfer
import org.echo.mobile.framework.model.Transaction
import org.echo.mobile.framework.model.contract.ContractInfo
import org.echo.mobile.framework.model.contract.ContractResult
import org.echo.mobile.framework.model.contract.ContractStruct
import org.echo.mobile.framework.model.network.Network
import org.echo.mobile.framework.model.socketoperations.BlockDataSocketOperation
import org.echo.mobile.framework.model.socketoperations.CancelAllSubscriptionsSocketOperation
import org.echo.mobile.framework.model.socketoperations.CustomOperation
import org.echo.mobile.framework.model.socketoperations.CustomSocketOperation
import org.echo.mobile.framework.model.socketoperations.FullAccountsSocketOperation
import org.echo.mobile.framework.model.socketoperations.GetAllContractsSocketOperation
import org.echo.mobile.framework.model.socketoperations.GetAssetsSocketOperation
import org.echo.mobile.framework.model.socketoperations.GetBlockSocketOperation
import org.echo.mobile.framework.model.socketoperations.GetChainIdSocketOperation
import org.echo.mobile.framework.model.socketoperations.GetContractLogsSocketOperation
import org.echo.mobile.framework.model.socketoperations.GetContractResultSocketOperation
import org.echo.mobile.framework.model.socketoperations.GetContractSocketOperation
import org.echo.mobile.framework.model.socketoperations.GetContractsSocketOperation
import org.echo.mobile.framework.model.socketoperations.GetGlobalPropertiesSocketOperation
import org.echo.mobile.framework.model.socketoperations.GetKeyReferencesSocketOperation
import org.echo.mobile.framework.model.socketoperations.GetObjectsSocketOperation
import org.echo.mobile.framework.model.socketoperations.GetSidechainTransfersSocketOperation
import org.echo.mobile.framework.model.socketoperations.ListAssetsSocketOperation
import org.echo.mobile.framework.model.socketoperations.LookupAssetsSymbolsSocketOperation
import org.echo.mobile.framework.model.socketoperations.QueryContractSocketOperation
import org.echo.mobile.framework.model.socketoperations.RequiredFeesSocketOperation
import org.echo.mobile.framework.model.socketoperations.SetSubscribeCallbackSocketOperation
import org.echo.mobile.framework.model.socketoperations.SubscribeContractLogsSocketOperation
import org.echo.mobile.framework.service.DatabaseApiService
import org.echo.mobile.framework.support.Result
import org.echo.mobile.framework.support.concurrent.future.FutureTask
import org.echo.mobile.framework.support.concurrent.future.completeCallback
import org.echo.mobile.framework.support.concurrent.future.wrapResult
import org.echo.mobile.framework.support.dematerialize
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
                    callback.onSuccess(result)
                }

                override fun onError(error: LocalException) {
                    callback.onError(
                        LocalException(
                            "Error occurred during accounts request",
                            error
                        )
                    )
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

    override fun getChainId(): Result<Exception, String> {
        val future = FutureTask<String>()
        getChainId(future.completeCallback())

        return future.wrapResult()
    }

    override fun getChainId(callback: Callback<String>) {
        val chainIdOperation = GetChainIdSocketOperation(
            id,
            callId = socketCoreComponent.currentId,
            callback = callback
        )
        socketCoreComponent.emit(chainIdOperation)
    }

    override fun getDynamicGlobalProperties(): Result<Exception, DynamicGlobalProperties> {
        val future = FutureTask<DynamicGlobalProperties>()
        getDynamicGlobalProperties(future.completeCallback())

        return future.wrapResult()
    }

    override fun getDynamicGlobalProperties(callback: Callback<DynamicGlobalProperties>) {
        val blockDataOperation = BlockDataSocketOperation(
            id,
            socketCoreComponent.currentId,
            callback
        )
        socketCoreComponent.emit(blockDataOperation)
    }

    override fun getGlobalProperties(callback: Callback<GlobalProperties>) {
        val blockDataOperation = GetGlobalPropertiesSocketOperation(
            id,
            socketCoreComponent.currentId,
            callback
        )
        socketCoreComponent.emit(blockDataOperation)
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

    override fun getAssets(assetIds: List<String>): Result<LocalException, List<Asset>> {
        val futureAssets = FutureTask<List<Asset>>()
        getAssets(assetIds, futureAssets.completeCallback())

        return futureAssets.wrapResult()
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

    override fun lookupAssetsSymbols(symbolsOrIds: List<String>, callback: Callback<List<Asset>>) {
        val operation = LookupAssetsSymbolsSocketOperation(
            id,
            symbolsOrIds.toTypedArray(),
            callId = socketCoreComponent.currentId,
            callback = callback
        )

        socketCoreComponent.emit(operation)
    }

    override fun lookupAssetsSymbols(symbolsOrIds: List<String>): Result<LocalException, List<Asset>> {
        val future = FutureTask<List<Asset>>()
        lookupAssetsSymbols(symbolsOrIds, future.completeCallback())

        return future.wrapResult()
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

    override fun getContractLogs(
        contractId: String,
        fromBlock: String,
        toBlock: String
    ): Result<LocalException, List<Log>> {
        val futureTask = FutureTask<List<Log>>()
        val operation = GetContractLogsSocketOperation(
            id,
            contractId, fromBlock, toBlock,
            callId = socketCoreComponent.currentId,
            callback = futureTask.completeCallback()
        )
        socketCoreComponent.emit(operation)

        return futureTask.wrapResult()
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

    override fun subscribeContractLogs(
        contractId: String,
        fromBlock: String,
        toBlock: String
    ): Result<LocalException, List<Log>> {
        val future = FutureTask<List<Log>>()
        val operation = SubscribeContractLogsSocketOperation(
            id,
            contractId,
            fromBlock,
            toBlock,
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

    override fun <T : GrapheneObject> getObjects(
        ids: List<String>,
        mapper: ObjectMapper<T>
    ): Result<Exception, List<T>> {
        val future = FutureTask<List<T>>()
        val operation = GetObjectsSocketOperation<T>(
            id,
            ids.toTypedArray(),
            mapper,
            callId = socketCoreComponent.currentId,
            callback = future.completeCallback()
        )
        socketCoreComponent.emit(operation)

        return future.wrapResult()
    }

    override fun <T> callCustomOperation(operation: CustomOperation<T>, callback: Callback<T>) {
        val customSocketOperation = CustomSocketOperation(
            id,
            socketCoreComponent.currentId,
            operation,
            callback
        )
        socketCoreComponent.emit(customSocketOperation)
    }

    override fun <T> callCustomOperation(operation: CustomOperation<T>): Result<LocalException, T> {
        val futureTask = FutureTask<T>()
        val customSocketOperation = CustomSocketOperation(
            id,
            socketCoreComponent.currentId,
            operation,
            futureTask.completeCallback()
        )
        socketCoreComponent.emit(customSocketOperation)

        return futureTask.wrapResult()
    }

    override fun getSidechainTransfers(
        ethAddress: String,
        callback: Callback<List<SidechainTransfer>>
    ) {
        val operation = GetSidechainTransfersSocketOperation(id, ethAddress, callback = callback)

        socketCoreComponent.emit(operation)
    }

    override fun getKeyReferences(keys: List<String>): Result<Exception, Map<String, List<String>>> {
        val futureTask = FutureTask<Map<String, List<String>>>()
        val getKeyReferencesSocketOperation = GetKeyReferencesSocketOperation(
            id,
            keys,
            network,
            socketCoreComponent.currentId,
            futureTask.completeCallback()
        )

        socketCoreComponent.emit(getKeyReferencesSocketOperation)

        return futureTask.wrapResult()
    }
}
