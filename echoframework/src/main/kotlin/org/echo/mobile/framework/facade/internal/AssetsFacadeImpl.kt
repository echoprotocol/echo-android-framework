package org.echo.mobile.framework.facade.internal

import com.google.common.primitives.UnsignedLong
import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.ECHO_ASSET_ID
import org.echo.mobile.framework.core.crypto.CryptoCoreComponent
import org.echo.mobile.framework.core.socket.SocketCoreComponent
import org.echo.mobile.framework.exception.LocalException
import org.echo.mobile.framework.facade.AssetsFacade
import org.echo.mobile.framework.model.*
import org.echo.mobile.framework.model.network.Network
import org.echo.mobile.framework.model.operations.CreateAssetOperation
import org.echo.mobile.framework.model.operations.IssueAssetOperationBuilder
import org.echo.mobile.framework.processResult
import org.echo.mobile.framework.service.DatabaseApiService
import org.echo.mobile.framework.service.NetworkBroadcastApiService
import org.echo.mobile.framework.support.concurrent.future.FutureTask
import org.echo.mobile.framework.support.concurrent.future.completeCallback
import org.echo.mobile.framework.support.dematerialize
import org.echo.mobile.framework.support.error
import org.echo.mobile.framework.support.value

/**
 * Implementation of [AssetsFacade]
 *
 * @author Dmitriy Bushuev
 */
class AssetsFacadeImpl(
    private val databaseApiService: DatabaseApiService,
    private val networkBroadcastApiService: NetworkBroadcastApiService,
    private val cryptoCoreComponent: CryptoCoreComponent,
    socketCoreComponent: SocketCoreComponent,
    network: Network
) : BaseNotifiedTransactionsFacade(
    databaseApiService,
    cryptoCoreComponent,
    socketCoreComponent,
    network
), AssetsFacade {

    override fun createAsset(
        name: String,
        password: String,
        asset: Asset,
        callback: Callback<String>
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

        val transaction = Transaction(blockData, listOf(operation), chainId).apply {
            setFees(fees)
            addPrivateKey(privateKey)
        }

        val callId =
            networkBroadcastApiService.broadcastTransactionWithCallback(transaction)
                .dematerialize()

        val future = FutureTask<TransactionResult>()
        subscribeOnTransactionResult(callId.toString(), future.completeCallback())

        future.get()?.trx?.operationsWithResults?.values?.firstOrNull()
            ?: throw LocalException("Result of contract creation not found.")
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

        val memoPrivateKey = memoKey(issuerNameOrId, password)
        operation.memo = generateMemo(memoPrivateKey, issuer!!, target!!, message)

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
