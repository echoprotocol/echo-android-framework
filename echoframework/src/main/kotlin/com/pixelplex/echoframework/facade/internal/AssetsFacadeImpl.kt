package com.pixelplex.echoframework.facade.internal

import com.pixelplex.echoframework.Callback
import com.pixelplex.echoframework.core.crypto.CryptoCoreComponent
import com.pixelplex.echoframework.exception.LocalException
import com.pixelplex.echoframework.facade.AssetsFacade
import com.pixelplex.echoframework.model.*
import com.pixelplex.echoframework.model.operations.CreateAssetOperation
import com.pixelplex.echoframework.service.DatabaseApiService
import com.pixelplex.echoframework.service.NetworkBroadcastApiService
import com.pixelplex.echoframework.support.Result
import com.pixelplex.echoframework.support.dematerialize
import com.pixelplex.echoframework.support.error
import com.pixelplex.echoframework.support.value

/**
 * Implementation of [AssetsFacade]
 *
 * @author Dmitriy Bushuev
 */
class AssetsFacadeImpl(
    private val databaseApiService: DatabaseApiService,
    private val networkBroadcastApiService: NetworkBroadcastApiService,
    private val cryptoCoreComponent: CryptoCoreComponent
) : AssetsFacade {

    override fun createAsset(
        name: String,
        password: String,
        asset: Asset,
        predictionMarket: Boolean,
        callback: Callback<Boolean>
    ) {
        Result {
            val operation = CreateAssetOperation(asset, predictionMarket)

            val blockData = databaseApiService.getBlockData()
            val chainId = getChainId()
            val fees = getFees(listOf(operation), "1.3.0")

            val privateKey =
                cryptoCoreComponent.getPrivateKey(
                    name,
                    password,
                    AuthorityType.ACTIVE
                )

            var account: Account? = null

            databaseApiService.getFullAccounts(listOf(name), false)
                .value { accountsMap ->
                    account = accountsMap[name]?.account
                            ?: throw LocalException("Unable to find required account $name")
                }
                .error { accountsError ->
                    throw LocalException("Error occurred during accounts request", accountsError)
                }

            checkOwnerAccount(account!!.name, password, account!!)

            val transaction = Transaction(privateKey, blockData, listOf(operation), chainId)
            transaction.setFees(fees)

            networkBroadcastApiService.broadcastTransactionWithCallback(transaction).dematerialize()
        }
            .value { result -> callback.onSuccess(result) }
            .error { error -> callback.onError(LocalException(error)) }
    }

    private fun checkOwnerAccount(name: String, password: String, account: Account) {
        val ownerAddress =
            cryptoCoreComponent.getAddress(name, password, AuthorityType.OWNER)

        val isKeySame = account.isEqualsByKey(ownerAddress, AuthorityType.OWNER)
        if (!isKeySame) {
            throw LocalException("Owner account checking exception")
        }
    }

    private fun getChainId(): String = databaseApiService.getChainId().dematerialize()

    private fun getFees(operations: List<BaseOperation>, asset: String): List<AssetAmount> =
        databaseApiService.getRequiredFees(operations, Asset(asset)).dematerialize()

    override fun listAssets(lowerBound: String, limit: Int, callback: Callback<List<Asset>>) =
        databaseApiService.listAssets(lowerBound, limit, callback)

    override fun getAssets(assetIds: List<String>, callback: Callback<List<Asset>>) {
        databaseApiService.getAssets(assetIds, callback)
    }

}
