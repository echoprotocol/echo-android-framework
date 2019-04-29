package org.echo.mobile.framework.facade.internal

import com.google.common.primitives.UnsignedLong
import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.core.crypto.CryptoCoreComponent
import org.echo.mobile.framework.facade.TransactionFacadeExtension
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
    private val networkBroadcastApiService: NetworkBroadcastApiService,
    override val cryptoCoreComponent: CryptoCoreComponent,
    override val databaseApiService: DatabaseApiService
) : TransactionsFacade, TransactionFacadeExtension {

    override fun sendTransferOperation(
        nameOrId: String,
        password: String,
        toNameOrId: String,
        amount: String,
        asset: String,
        feeAsset: String?,
        message: String?,
        callback: Callback<Boolean>
    ) = callback.processResult {
        val (fromAccount, toAccount) = getParticipantsPair(nameOrId, toNameOrId)

        checkOwnerAccount(fromAccount.name, password, fromAccount)

        val privateKey = cryptoCoreComponent.getPrivateKey(
            fromAccount.name,
            password,
            AuthorityType.ACTIVE
        )

        val memoPrivateKey = memoKey(fromAccount.name, password)
        val memo = generateMemo(memoPrivateKey, fromAccount, toAccount, message)

        val transfer = TransferOperationBuilder()
            .setFrom(fromAccount)
            .setTo(toAccount)
            .setAmount(AssetAmount(UnsignedLong.valueOf(amount.toLong()), Asset(asset)))
            .setMemo(memo)
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
        message: String?,
        callback: Callback<Boolean>
    ) = callback.processResult {
        val (fromAccount, toAccount) = getParticipantsPair(nameOrId, toNameOrId)

        checkOwnerAccount(wif, fromAccount)

        val privateKey = cryptoCoreComponent.decodeFromWif(wif)
        val memo = generateMemo(privateKey, fromAccount, toAccount, message)

        val transfer = TransferOperationBuilder()
            .setFrom(fromAccount)
            .setTo(toAccount)
            .setAmount(AssetAmount(UnsignedLong.valueOf(amount.toLong()), Asset(asset)))
            .setMemo(memo)
            .build()

        val transaction = configureTransaction(transfer, privateKey, asset, feeAsset)

        networkBroadcastApiService.broadcastTransaction(transaction).dematerialize()
    }

}
