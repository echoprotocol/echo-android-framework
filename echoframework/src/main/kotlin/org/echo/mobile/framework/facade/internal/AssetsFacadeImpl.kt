package org.echo.mobile.framework.facade.internal

import com.google.common.primitives.UnsignedLong
import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.ECHO_ASSET_ID
import org.echo.mobile.framework.core.crypto.CryptoCoreComponent
import org.echo.mobile.framework.exception.AccountNotFoundException
import org.echo.mobile.framework.exception.LocalException
import org.echo.mobile.framework.exception.NotFoundException
import org.echo.mobile.framework.facade.AssetsFacade
import org.echo.mobile.framework.model.Account
import org.echo.mobile.framework.model.Asset
import org.echo.mobile.framework.model.AssetAmount
import org.echo.mobile.framework.model.Transaction
import org.echo.mobile.framework.model.TransactionResult
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
    private val notifiedTransactionsHelper: NotificationsHelper<TransactionResult>,
    private val transactionExpirationDelay: Long
) : BaseTransactionsFacade(databaseApiService, cryptoCoreComponent, transactionExpirationDelay),
    AssetsFacade {

    override fun createAsset(
        name: String,
        wif: String,
        asset: Asset,
        broadcastCallback: Callback<Boolean>,
        resultCallback: Callback<String>?
    ) {
        val callId: String
        try {
            val privateKey = cryptoCoreComponent.decodeFromWif(wif)

            val account = findAccount(name)

            checkOwnerAccount(wif, account)
            callId = createAsset(privateKey, asset)

            broadcastCallback.onSuccess(true)
        } catch (ex: Exception) {
            broadcastCallback.onError(ex as? LocalException ?: LocalException(ex))
            return
        }

        resultCallback?.let {
            retrieveTransactionResult(callId, it)
        }
    }

    private fun createAsset(
        privateKey: ByteArray,
        asset: Asset
    ): String {
        val operation = CreateAssetOperation(asset)

        val transaction = configureTransaction(operation, privateKey, ECHO_ASSET_ID)

        return networkBroadcastApiService.broadcastTransactionWithCallback(transaction)
            .dematerialize().toString()
    }

    private fun retrieveTransactionResult(callId: String, callback: Callback<String>) {
        try {
            val future = FutureTask<TransactionResult>()
            notifiedTransactionsHelper.subscribeOnResult(
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
        wif: String,
        asset: String,
        amount: String,
        destinationIdOrName: String,
        callback: Callback<Boolean>
    ) = callback.processResult {
        val (issuer, target) = getParticipantsPair(issuerNameOrId, destinationIdOrName)

        checkOwnerAccount(wif, issuer)

        val operation = IssueAssetOperationBuilder()
            .setIssuer(issuer)
            .setAmount(AssetAmount(UnsignedLong.valueOf(amount.toLong()), Asset(asset)))
            .setDestination(target)
            .build()

        val privateKey = cryptoCoreComponent.decodeFromWif(wif)

        val transaction = configureTransaction(operation, privateKey, asset, ECHO_ASSET_ID)

        networkBroadcastApiService.broadcastTransaction(transaction).dematerialize()
    }

    override fun listAssets(lowerBound: String, limit: Int, callback: Callback<List<Asset>>) =
        databaseApiService.listAssets(lowerBound, limit, callback)

    override fun getAssets(assetIds: List<String>, callback: Callback<List<Asset>>) =
        databaseApiService.getAssets(assetIds, callback)

    override fun lookupAssetsSymbols(symbolsOrIds: List<String>, callback: Callback<List<Asset>>) =
        databaseApiService.lookupAssetsSymbols(symbolsOrIds, callback)

    private fun findAccount(nameOrId: String): Account {
        val accountsMap =
            databaseApiService.getFullAccounts(listOf(nameOrId), false).dematerialize()
        return accountsMap[nameOrId]?.account
            ?: throw AccountNotFoundException("Unable to find required account $nameOrId")
    }
}
