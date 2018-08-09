package com.pixelplex.echoframework.facade.internal

import com.google.common.primitives.UnsignedLong
import com.pixelplex.echoframework.BITSHARES_ASSET_ID
import com.pixelplex.echoframework.Callback
import com.pixelplex.echoframework.core.crypto.CryptoCoreComponent
import com.pixelplex.echoframework.exception.LocalException
import com.pixelplex.echoframework.facade.AssetsFacade
import com.pixelplex.echoframework.model.*
import com.pixelplex.echoframework.model.operations.CreateAssetOperation
import com.pixelplex.echoframework.model.operations.IssueAssetOperation
import com.pixelplex.echoframework.service.DatabaseApiService
import com.pixelplex.echoframework.service.NetworkBroadcastApiService
import com.pixelplex.echoframework.support.Result
import com.pixelplex.echoframework.support.dematerialize
import com.pixelplex.echoframework.support.error
import com.pixelplex.echoframework.support.value
import java.math.BigInteger

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
        callback: Callback<Boolean>
    ) {
        Result {
            val operation = CreateAssetOperation(asset)

            val blockData = databaseApiService.getBlockData()
            val chainId = getChainId()
            val fees = getFees(listOf(operation), BITSHARES_ASSET_ID)

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

    override fun issueAsset(
        issuerNameOrId: String,
        password: String,
        asset: String,
        amount: String,
        destinationIdOrName: String,
        message: String?,
        callback: Callback<Boolean>
    ) {
        Result {
            var issuer: Account? = null
            var target: Account? = null

            databaseApiService.getFullAccounts(listOf(issuerNameOrId, destinationIdOrName), false)
                .value { accountsMap ->
                    issuer = accountsMap[issuerNameOrId]?.account
                            ?:
                            throw LocalException("Unable to find required account $issuerNameOrId")
                    target = accountsMap[destinationIdOrName]?.account
                            ?:
                            throw LocalException("Unable to find required account $destinationIdOrName")
                }
                .error { accountsError ->
                    throw LocalException("Error occurred during accounts request", accountsError)
                }

            checkOwnerAccount(issuer!!.name, password, issuer!!)

            val blockData = databaseApiService.getBlockData()
            val chainId = getChainId()

            val operation = IssueAssetOperation(
                issuer!!,
                AssetAmount(UnsignedLong.valueOf(amount.toLong()), Asset(asset)),
                target!!
            )

            val privateKey =
                cryptoCoreComponent.getPrivateKey(
                    issuerNameOrId,
                    password,
                    AuthorityType.ACTIVE
                )

            operation.memo = generateMemo(privateKey, issuer!!, target!!, message)

            val fees = getFees(listOf(operation), BITSHARES_ASSET_ID)

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

    private fun generateMemo(
        privateKey: ByteArray,
        fromAccount: Account,
        toAccount: Account,
        message: String?
    ): Memo {
        if (message != null) {
            val encryptedMessage = cryptoCoreComponent.encryptMessage(
                privateKey,
                toAccount.options.memoKey!!.key,
                BigInteger.ZERO,
                message
            )

            return Memo(
                Address(fromAccount.options.memoKey!!),
                Address(toAccount.options.memoKey!!),
                BigInteger.ZERO,
                encryptedMessage ?: ByteArray(0)
            )
        }

        return Memo()
    }

    override fun listAssets(lowerBound: String, limit: Int, callback: Callback<List<Asset>>) =
        databaseApiService.listAssets(lowerBound, limit, callback)

    override fun getAssets(assetIds: List<String>, callback: Callback<List<Asset>>) {
        databaseApiService.getAssets(assetIds, callback)
    }

}
