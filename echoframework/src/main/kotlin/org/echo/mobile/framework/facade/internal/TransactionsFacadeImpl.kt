package org.echo.mobile.framework.facade.internal

import com.google.common.primitives.UnsignedLong
import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.core.crypto.CryptoCoreComponent
import org.echo.mobile.framework.facade.TransactionsFacade
import org.echo.mobile.framework.model.Asset
import org.echo.mobile.framework.model.AssetAmount
import org.echo.mobile.framework.model.AuthorityType
import org.echo.mobile.framework.model.operations.TransferOperationBuilder
import org.echo.mobile.framework.processResult
import org.echo.mobile.framework.service.DatabaseApiService
import org.echo.mobile.framework.service.NetworkBroadcastApiService
import org.echo.mobile.framework.support.dematerialize

/**
 * Implementation of [TransactionsFacade]
 *
 * Delegates API call logic to [DatabaseApiService] and [NetworkBroadcastApiService]
 *
 * @author Dmitriy Bushuev
 */
class TransactionsFacadeImpl(
    private val databaseApiService: DatabaseApiService,
    private val networkBroadcastApiService: NetworkBroadcastApiService,
    private val cryptoCoreComponent: CryptoCoreComponent
) : BaseTransactionsFacade(databaseApiService, cryptoCoreComponent), TransactionsFacade {

    override fun sendTransferOperation(
        nameOrId: String,
        password: String,
        toNameOrId: String,
        amount: String,
        asset: String,
        feeAsset: String?,
        callback: Callback<Boolean>
    ) = callback.processResult {
        val (fromAccount, toAccount) = getParticipantsPair(nameOrId, toNameOrId)

        checkOwnerAccount(fromAccount.name, password, fromAccount)

        val privateKey = cryptoCoreComponent.getEdDSAPrivateKey(
            fromAccount.name,
            password,
            AuthorityType.ACTIVE
        )

        val transfer = TransferOperationBuilder()
            .setFrom(fromAccount)
            .setTo(toAccount)
            .setAmount(AssetAmount(UnsignedLong.valueOf(amount.toLong()), Asset(asset)))
            .build()

        val transaction = configureTransaction(transfer, privateKey, asset, feeAsset)

        networkBroadcastApiService.broadcastTransaction(transaction).dematerialize()
    }

    override fun sendTransferOperationWithWif(
        nameOrId: String,
        wif: String,
        toNameOrId: String,
        amount: String,
        asset: String,
        feeAsset: String?,
        callback: Callback<Boolean>
    ) = callback.processResult {
        val (fromAccount, toAccount) = getParticipantsPair(nameOrId, toNameOrId)

        checkOwnerAccount(wif, fromAccount)

        val privateKey = cryptoCoreComponent.decodeFromWif(wif)

        val transfer = TransferOperationBuilder()
            .setFrom(fromAccount)
            .setTo(toAccount)
            .setAmount(AssetAmount(UnsignedLong.valueOf(amount.toLong()), Asset(asset)))
            .build()

        val transaction = configureTransaction(transfer, privateKey, asset, feeAsset)

        networkBroadcastApiService.broadcastTransaction(transaction).dematerialize()
    }

}
