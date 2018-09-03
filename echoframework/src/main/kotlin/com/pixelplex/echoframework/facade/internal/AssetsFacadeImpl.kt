package com.pixelplex.echoframework.facade.internal

import com.google.common.primitives.UnsignedLong
import com.pixelplex.echoframework.Callback
import com.pixelplex.echoframework.ECHO_ASSET_ID
import com.pixelplex.echoframework.core.crypto.CryptoCoreComponent
import com.pixelplex.echoframework.exception.LocalException
import com.pixelplex.echoframework.facade.AssetsFacade
import com.pixelplex.echoframework.model.*
import com.pixelplex.echoframework.model.operations.CreateAssetOperation
import com.pixelplex.echoframework.model.operations.IssueAssetOperationBuilder
import com.pixelplex.echoframework.processResult
import com.pixelplex.echoframework.service.DatabaseApiService
import com.pixelplex.echoframework.service.NetworkBroadcastApiService
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
) : BaseTransactionsFacade(databaseApiService, cryptoCoreComponent), AssetsFacade {

    override fun createAsset(
        name: String,
        password: String,
        asset: Asset,
        callback: Callback<Boolean>
    ) = callback.processResult {
        val operation = CreateAssetOperation(asset)

        val blockData = databaseApiService.getBlockData()
        val chainId = getChainId()
        val fees = getFees(listOf(operation), ECHO_ASSET_ID)

        val privateKey = cryptoCoreComponent.getPrivateKey(
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

    override fun issueAsset(
        issuerNameOrId: String,
        password: String,
        asset: String,
        amount: String,
        destinationIdOrName: String,
        message: String?,
        callback: Callback<Boolean>
    ) = callback.processResult {
        var issuer: Account? = null
        var target: Account? = null

        databaseApiService.getFullAccounts(listOf(issuerNameOrId, destinationIdOrName), false)
            .value { accountsMap ->
                issuer = accountsMap[issuerNameOrId]?.account
                        ?: throw LocalException("Unable to find required account $issuerNameOrId")
                target = accountsMap[destinationIdOrName]?.account
                        ?:
                        throw LocalException("Unable to find required account $destinationIdOrName")
            }
            .error { accountsError ->
                throw LocalException("Error occurred during accounts request", accountsError)
            }

        checkOwnerAccount(issuer!!.name, password, issuer!!)

        val operation = IssueAssetOperationBuilder()
            .setIssuer(issuer!!)
            .setAmount(AssetAmount(UnsignedLong.valueOf(amount.toLong()), Asset(asset)))
            .setDestination(target!!)
            .build()

        val privateKey =
            cryptoCoreComponent.getPrivateKey(
                issuerNameOrId,
                password,
                AuthorityType.ACTIVE
            )

        val memoPrivateKey =
            cryptoCoreComponent.getPrivateKey(
                issuerNameOrId,
                password,
                AuthorityType.KEY
            )

        operation.memo = generateMemo(memoPrivateKey, issuer!!, target!!, message)

        val blockData = databaseApiService.getBlockData()
        val chainId = getChainId()
        val fees = getFees(listOf(operation), ECHO_ASSET_ID)

        val transaction = Transaction(privateKey, blockData, listOf(operation), chainId).apply {
            setFees(fees)
        }

        networkBroadcastApiService.broadcastTransactionWithCallback(transaction).dematerialize()
    }

    override fun listAssets(lowerBound: String, limit: Int, callback: Callback<List<Asset>>) =
        databaseApiService.listAssets(lowerBound, limit, callback)

    override fun getAssets(assetIds: List<String>, callback: Callback<List<Asset>>) {
        databaseApiService.getAssets(assetIds, callback)
    }

}
