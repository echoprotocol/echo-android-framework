package org.echo.mobile.framework.facade.internal

import com.google.common.primitives.UnsignedLong
import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.core.crypto.CryptoCoreComponent
import org.echo.mobile.framework.exception.LocalException
import org.echo.mobile.framework.exception.NotFoundException
import org.echo.mobile.framework.facade.TransactionsFacade
import org.echo.mobile.framework.model.Asset
import org.echo.mobile.framework.model.AssetAmount
import org.echo.mobile.framework.model.TransactionResult
import org.echo.mobile.framework.model.operations.TransferOperationBuilder
import org.echo.mobile.framework.model.socketoperations.ResultCallback
import org.echo.mobile.framework.service.DatabaseApiService
import org.echo.mobile.framework.service.NetworkBroadcastApiService
import org.echo.mobile.framework.support.concurrent.future.FutureTask
import org.echo.mobile.framework.support.concurrent.future.completeCallback
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
        private val cryptoCoreComponent: CryptoCoreComponent,
        private val notifiedTransactionsHelper: NotificationsHelper<TransactionResult>,
        private val transactionExpirationDelay: Long
) : BaseTransactionsFacade(databaseApiService, cryptoCoreComponent, transactionExpirationDelay),
        TransactionsFacade {

    override fun sendTransferOperation(
            nameOrId: String,
            wif: String,
            toNameOrId: String,
            amount: String,
            asset: String,
            feeAsset: String?,
            broadcastCallback: Callback<Boolean>,
            resultCallback: ResultCallback<TransactionResult>
    ) {
        val callId: String
        try {
            val (fromAccount, toAccount) = getParticipantsPair(nameOrId, toNameOrId)

            checkOwnerAccount(wif, fromAccount)

            val privateKey = cryptoCoreComponent.decodeFromWif(wif)

            val transfer = TransferOperationBuilder()
                    .setFrom(fromAccount)
                    .setTo(toAccount)
                    .setAmount(AssetAmount(UnsignedLong.valueOf(amount.toLong()), Asset(asset)))
                    .build()

            val transaction = configureTransaction(transfer, privateKey, asset, feeAsset)

            callId = networkBroadcastApiService.broadcastTransactionWithCallback(transaction)
                    .dematerialize().toString()
            broadcastCallback.onSuccess(true)
        } catch (ex: Exception) {
            broadcastCallback.onError(ex as? LocalException ?: LocalException(ex))
            return
        }

        retrieveTransactionResult(callId, resultCallback.get())
    }


    private fun retrieveTransactionResult(
            callId: String,
            callback: Callback<TransactionResult>
    ) {
        try {
            val future = FutureTask<TransactionResult>()
            notifiedTransactionsHelper.subscribeOnResult(
                    callId,
                    future.completeCallback()
            )

            val result = future.get()
                    ?: throw NotFoundException("Result of operation not found.")

            callback.onSuccess(result)
        } catch (ex: Exception) {
            callback.onError(ex as? LocalException ?: LocalException(ex))
        }
    }
}
