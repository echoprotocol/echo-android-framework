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
import org.echo.mobile.framework.model.EthAddress
import org.echo.mobile.framework.model.EthDeposit
import org.echo.mobile.framework.model.EthWithdraw
import org.echo.mobile.framework.model.FullAccount
import org.echo.mobile.framework.model.GlobalProperties
import org.echo.mobile.framework.model.GrapheneObject
import org.echo.mobile.framework.model.Log
import org.echo.mobile.framework.model.Transaction
import org.echo.mobile.framework.model.contract.ContractFee
import org.echo.mobile.framework.model.contract.ContractInfo
import org.echo.mobile.framework.model.contract.ContractResult
import org.echo.mobile.framework.model.contract.ContractStruct
import org.echo.mobile.framework.model.network.Network
import org.echo.mobile.framework.model.socketoperations.BlockDataSocketOperation
import org.echo.mobile.framework.model.socketoperations.CancelAllSubscriptionsSocketOperation
import org.echo.mobile.framework.model.socketoperations.CustomOperation
import org.echo.mobile.framework.model.socketoperations.CustomSocketOperation
import org.echo.mobile.framework.model.socketoperations.FullAccountsSocketOperation
import org.echo.mobile.framework.model.socketoperations.GetAccountDepositsSocketOperation
import org.echo.mobile.framework.model.socketoperations.GetAccountWithdrawalsSocketOperation
import org.echo.mobile.framework.model.socketoperations.GetAssetsSocketOperation
import org.echo.mobile.framework.model.socketoperations.GetBlockSocketOperation
import org.echo.mobile.framework.model.socketoperations.GetChainIdSocketOperation
import org.echo.mobile.framework.model.socketoperations.GetContractLogsSocketOperation
import org.echo.mobile.framework.model.socketoperations.GetContractResultSocketOperation
import org.echo.mobile.framework.model.socketoperations.GetContractSocketOperation
import org.echo.mobile.framework.model.socketoperations.GetContractsSocketOperation
import org.echo.mobile.framework.model.socketoperations.GetEthereumAddressSocketOperation
import org.echo.mobile.framework.model.socketoperations.GetGlobalPropertiesSocketOperation
import org.echo.mobile.framework.model.socketoperations.GetKeyReferencesSocketOperation
import org.echo.mobile.framework.model.socketoperations.GetObjectsSocketOperation
import org.echo.mobile.framework.model.socketoperations.ListAssetsSocketOperation
import org.echo.mobile.framework.model.socketoperations.LookupAssetsSymbolsSocketOperation
import org.echo.mobile.framework.model.socketoperations.QueryContractSocketOperation
import org.echo.mobile.framework.model.socketoperations.RequiredContractFeesSocketOperation
import org.echo.mobile.framework.model.socketoperations.RequiredFeesSocketOperation
import org.echo.mobile.framework.model.socketoperations.SetSubscribeCallbackSocketOperation
import org.echo.mobile.framework.model.socketoperations.SubscribeContractLogsSocketOperation
import org.echo.mobile.framework.model.socketoperations.SubscribeContractsSocketOperation
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
    private val cryptoCoreComponent: CryptoCoreComponent,
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

    override fun getAccountsByWif(
        wifs: List<String>,
        callback: Callback<Map<String, List<FullAccount>>>
    ) {
        val keys = wifs.map { wif ->
            val privateKey = cryptoCoreComponent.decodeFromWif(wif)
            val publicKey = cryptoCoreComponent.deriveEdDSAPublicKeyFromPrivate(privateKey)
            cryptoCoreComponent.getEdDSAAddressFromPublicKey(publicKey)
        }

        val getKeyReferencesSocketOperation = GetKeyReferencesSocketOperation(
            id,
            keys,
            network,
            socketCoreComponent.currentId,
            object : Callback<Map<String, List<String>>> {
                override fun onSuccess(result: Map<String, List<String>>) {
                    val requiredAccounts = result.values.flatten()

                    receiveRequiredAccountsByWifs(wifs, keys, requiredAccounts, result, callback)
                }

                override fun onError(error: LocalException) {
                    callback.onError(error)
                }

            }
        )

        socketCoreComponent.emit(getKeyReferencesSocketOperation)
    }

    private fun receiveRequiredAccountsByWifs(
        wifs: List<String>,
        keys: List<String>,
        requiredAccounts: List<String>,
        accountsIdsMap: Map<String, List<String>>,
        callback: Callback<Map<String, List<FullAccount>>>
    ) {
        getFullAccounts(
            requiredAccounts,
            false,
            object : Callback<Map<String, FullAccount>> {
                override fun onSuccess(result: Map<String, FullAccount>) {
                    val resultMap =
                        accountsByWifMap(wifs, keys, accountsIdsMap, result)

                    callback.onSuccess(resultMap)
                }

                override fun onError(error: LocalException) {
                    callback.onSuccess(mapOf())
                }
            })
    }

    override fun getEthereumAddress(
        accountId: String,
        callback: Callback<EthAddress>
    ) {
        val operation = GetEthereumAddressSocketOperation(
            id,
            accountId,
            socketCoreComponent.currentId,
            callback
        )

        socketCoreComponent.emit(operation)
    }

    private fun accountsByWifMap(
        wifs: List<String>,
        keys: List<String>,
        accountsIdsMap: Map<String, List<String>>,
        receivedAccounts: Map<String, FullAccount>
    ): Map<String, List<FullAccount>> {
        val resultMap = hashMapOf<String, List<FullAccount>>()

        accountsIdsMap.forEach { (key, accountIds) ->
            val wif = wifs[keys.indexOf(key)]
            val accounts = mutableListOf<FullAccount>()

            accountIds.distinct().forEach { id ->
                val account = receivedAccounts[id]
                account?.let { accounts.add(it) }
            }

            resultMap[wif] = accounts
        }

        return resultMap
    }

    override fun getAccountsByWif(wifs: List<String>): Result<LocalException, Map<String, List<FullAccount>>> {
        val accountFuture = FutureTask<Map<String, List<FullAccount>>>()

        getAccountsByWif(wifs, accountFuture.completeCallback())

        return accountFuture.wrapResult()
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
        if (assets.isEmpty()) {
            callback.onSuccess(accounts)
            return
        }

        accounts.values.forEach { fullAccount ->
            fullAccount.balances?.forEach { balance ->
                balance.asset =
                    assets.find { asset -> asset.getObjectId() == balance.asset?.getObjectId() }
                        ?: balance.asset
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

    override fun getBlockData(): BlockData {
        val dynamicProperties = getDynamicGlobalProperties().dematerialize()
        val expirationTime = TimeUnit.MILLISECONDS.toSeconds(dynamicProperties.date!!.time) +
                Transaction.DEFAULT_EXPIRATION_TIME
        val headBlockId = dynamicProperties.headBlockId
        val headBlockNumber = dynamicProperties.headBlockNumber
        return BlockData(headBlockNumber, headBlockId, expirationTime)
    }

    override fun getBlockData(callback: Callback<BlockData>) {
        callback.onSuccess(getBlockData())
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

    override fun getRequiredContractFees(
        operations: List<BaseOperation>,
        asset: Asset
    ): Result<Exception, List<ContractFee>> {
        val future = FutureTask<List<ContractFee>>()
        val requiredFeesOperation = RequiredContractFeesSocketOperation(
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
        limit: String
    ): Result<LocalException, List<Log>> {
        val future = FutureTask<List<Log>>()
        val operation = SubscribeContractLogsSocketOperation(
            id,
            contractId,
            fromBlock,
            limit,
            callId = socketCoreComponent.currentId,
            callback = future.completeCallback()
        )
        socketCoreComponent.emit(operation)

        return future.wrapResult()
    }

    override fun subscribeContracts(contractIds: List<String>): Result<LocalException, Boolean> {
        val future = FutureTask<Boolean>()

        val operation = SubscribeContractsSocketOperation(
            id,
            contractIds,
            callId = socketCoreComponent.currentId,
            callback = future.completeCallback()
        )
        socketCoreComponent.emit(operation)

        return future.wrapResult()
    }

    override fun subscribe(clearFilter: Boolean, callback: Callback<Boolean>) {
        socketCoreComponent.emit(createSubscriptionOperation(clearFilter, callback))
    }

    override fun getAccountDeposits(accountId: String, callback: Callback<List<EthDeposit>>) {
        val operation = GetAccountDepositsSocketOperation(
            id,
            accountId,
            socketCoreComponent.currentId,
            callback
        )

        socketCoreComponent.emit(operation)
    }

    override fun getAccountWithdrawals(accountId: String, callback: Callback<List<EthWithdraw>>) {
        val operation = GetAccountWithdrawalsSocketOperation(
            id,
            accountId,
            socketCoreComponent.currentId,
            callback
        )

        socketCoreComponent.emit(operation)
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
}
