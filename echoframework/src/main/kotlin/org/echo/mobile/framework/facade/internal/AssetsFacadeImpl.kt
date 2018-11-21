package org.echo.mobile.framework.facade.internal

import com.google.common.primitives.UnsignedLong
import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.ECHO_ASSET_ID
import org.echo.mobile.framework.core.crypto.CryptoCoreComponent
import org.echo.mobile.framework.exception.LocalException
import org.echo.mobile.framework.exception.NotFoundException
import org.echo.mobile.framework.facade.AssetsFacade
import org.echo.mobile.framework.model.*
import org.echo.mobile.framework.model.operations.CreateAssetOperation
import org.echo.mobile.framework.model.operations.IssueAssetOperationBuilder
import org.echo.mobile.framework.processResult
import org.echo.mobile.framework.service.DatabaseApiService
import org.echo.mobile.framework.service.NetworkBroadcastApiService
import org.echo.mobile.framework.support.concurrent.future.FutureTask
import org.echo.mobile.framework.support.concurrent.future.completeCallback
import org.echo.mobile.framework.support.dematerialize

/**
 * Implementation of [AssetsFacade]
 *
 * @author Dmitriy Bushuev
 */
class AssetsFacadeImpl(
    private val databaseApiService: DatabaseApiService,
    private val networkBroadcastApiService: NetworkBroadcastApiService,
    private val cryptoCoreComponent: CryptoCoreComponent,
    private val notifiedTransactionsHelper: NotifiedTransactionsHelper
) : BaseTransactionsFacade(databaseApiService, cryptoCoreComponent), AssetsFacade {

    override fun createAsset(
        name: String,
        password: String,
        asset: Asset,
        broadcastCallback: Callback<Boolean>,
        resultCallback: Callback<String>?
    ) {

        val callId: String
        try {
            val privateKey = cryptoCoreComponent.getPrivateKey(
                name, password, AuthorityType.ACTIVE
            )

            val accountsMap =
                databaseApiService.getFullAccounts(listOf(name), false).dematerialize()

            val account = accountsMap[name]?.account
                ?: throw NotFoundException("Unable to find required account $name")

            checkOwnerAccount(account.name, password, account)

            val blockData = databaseApiService.getBlockData()
            val chainId = getChainId()

            val operation = CreateAssetOperation(asset)
            val fees = getFees(listOf(operation), ECHO_ASSET_ID)

            val transaction = Transaction(blockData, listOf(operation), chainId).apply {
                setFees(fees)
                addPrivateKey(privateKey)
            }

            callId = networkBroadcastApiService.broadcastTransactionWithCallback(transaction)
                .dematerialize().toString()

            broadcastCallback.onSuccess(true)

        } catch (ex: Exception) {
            broadcastCallback.onError(ex as? LocalException ?: LocalException(ex))
            return
        }

        resultCallback?.let {
            retrieveTransactionResult(callId, it)
        }
    }

    private fun retrieveTransactionResult(callId: String, callback: Callback<String>) {
        try {
            val future = FutureTask<TransactionResult>()
            notifiedTransactionsHelper.subscribeOnTransactionResult(
                callId,
                future.completeCallback()
            )

            val result = future.get()?.trx?.operationsWithResults?.values?.firstOrNull()
                ?: throw NotFoundException("Result of asset creation not found.")

            callback.onSuccess(result)
        } catch (ex: Exception) {
            callback.onError(ex as? LocalException ?: LocalException(ex))
        }
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
        val accountsMap =
            databaseApiService.getFullAccounts(listOf(issuerNameOrId, destinationIdOrName), false)
                .dematerialize()

        val issuer = accountsMap[issuerNameOrId]?.account
            ?: throw NotFoundException("Unable to find required account $issuerNameOrId")

        val target = accountsMap[destinationIdOrName]?.account
            ?: throw NotFoundException("Unable to find required account $destinationIdOrName")

        checkOwnerAccount(issuer.name, password, issuer)

        val operation = IssueAssetOperationBuilder()
            .setIssuer(issuer)
            .setAmount(AssetAmount(UnsignedLong.valueOf(amount.toLong()), Asset(asset)))
            .setDestination(target)
            .build()

        val privateKey = cryptoCoreComponent.getPrivateKey(
            issuerNameOrId,
            password,
            AuthorityType.ACTIVE
        )

        val memoPrivateKey = memoKey(issuerNameOrId, password)
        operation.memo = generateMemo(memoPrivateKey, issuer, target, message)

        val blockData = databaseApiService.getBlockData()
        val chainId = getChainId()
        val fees = getFees(listOf(operation), ECHO_ASSET_ID)

        val transaction = Transaction(blockData, listOf(operation), chainId).apply {
            setFees(fees)
            addPrivateKey(privateKey)
        }

        networkBroadcastApiService.broadcastTransaction(transaction).dematerialize()
    }

    override fun listAssets(lowerBound: String, limit: Int, callback: Callback<List<Asset>>) =
        databaseApiService.listAssets(lowerBound, limit, callback)

    override fun getAssets(assetIds: List<String>, callback: Callback<List<Asset>>) {
        databaseApiService.getAssets(assetIds, callback)
    }

}
