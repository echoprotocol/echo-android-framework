package org.echo.mobile.framework.facade.internal

import com.google.common.primitives.UnsignedLong
import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.ECHO_ASSET_ID
import org.echo.mobile.framework.core.crypto.CryptoCoreComponent
import org.echo.mobile.framework.exception.AccountNotFoundException
import org.echo.mobile.framework.exception.LocalException
import org.echo.mobile.framework.exception.NotFoundException
import org.echo.mobile.framework.facade.BitcoinSidechainFacade
import org.echo.mobile.framework.facade.EthereumSidechainFacade
import org.echo.mobile.framework.model.Account
import org.echo.mobile.framework.model.BtcAddress
import org.echo.mobile.framework.model.operations.GenerateBitcoinAddressOperation
import org.echo.mobile.framework.model.Transaction
import org.echo.mobile.framework.model.TransactionResult
import org.echo.mobile.framework.model.operations.WithdrawBitcoinOperation
import org.echo.mobile.framework.service.DatabaseApiService
import org.echo.mobile.framework.service.NetworkBroadcastApiService
import org.echo.mobile.framework.support.concurrent.future.FutureTask
import org.echo.mobile.framework.support.concurrent.future.completeCallback
import org.echo.mobile.framework.support.dematerialize

/**
 * Implementation of [EthereumSidechainFacade]
 *
 * Wraps logic for sidechain processing
 *
 * @author Dmitriy Bushuev
 */
class BitcoinSidechainFacadeImpl(
    private val databaseApiService: DatabaseApiService,
    private val networkBroadcastApiService: NetworkBroadcastApiService,
    private val cryptoCoreComponent: CryptoCoreComponent,
    private val notifiedTransactionsHelper: NotificationsHelper<TransactionResult>,
    private val transactionExpirationDelay: Long
) :
    BaseTransactionsFacade(databaseApiService, cryptoCoreComponent, transactionExpirationDelay),
    BitcoinSidechainFacade {

    override fun generateBitcoinAddress(
        accountNameOrId: String,
        wif: String,
        backupAddress: String,
        broadcastCallback: Callback<Boolean>,
        resultCallback: Callback<TransactionResult>?
    ) {
        val callId: String
        try {
            val account = findAccount(accountNameOrId)
            checkOwnerAccount(wif, account)
            val privateKey = cryptoCoreComponent.decodeFromWif(wif)

            callId = generateAddress(account, backupAddress, privateKey)

            broadcastCallback.onSuccess(true)
        } catch (exception: Exception) {
            broadcastCallback.onError(exception as? LocalException ?: LocalException(exception))
            return
        }

        resultCallback?.let {
            retrieveTransactionResult(callId, it)
        }
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

    override fun getBitcoinAddress(
        accountNameOrId: String,
        callback: Callback<BtcAddress>
    ) {
        val id = findAccount(accountNameOrId).getObjectId()
        databaseApiService.getBitcoinAddress(id, callback)
    }

    override fun btcWithdraw(
        accountNameOrId: String,
        wif: String,
        btcAddress: String,
        value: String,
        feeAsset: String,
        broadcastCallback: Callback<Boolean>,
        resultCallback: Callback<TransactionResult>?
    ) {
        val callId: String
        try {
            val account = findAccount(accountNameOrId)
            checkOwnerAccount(wif, account)
            val privateKey = cryptoCoreComponent.decodeFromWif(wif)

            callId = withdraw(btcAddress, account, privateKey, value, feeAsset)

            broadcastCallback.onSuccess(true)
        } catch (exception: Exception) {
            broadcastCallback.onError(exception as? LocalException ?: LocalException(exception))
            return
        }

        resultCallback?.let {
            retrieveTransactionResult(callId, it)
        }
    }

    private fun withdraw(
        btcAddress: String,
        account: Account,
        privateKey: ByteArray,
        value: String,
        feeAsset: String
    ): String {
        val operation =
            WithdrawBitcoinOperation(
                Account(account.getObjectId()),
                btcAddress,
                UnsignedLong.valueOf(value)
            )

        val transaction = configureTransaction(operation, privateKey, feeAsset)

        return networkBroadcastApiService.broadcastTransactionWithCallback(transaction)
            .dematerialize().toString()
    }

    private fun generateAddress(
        account: Account,
        backupAddress: String,
        privateKey: ByteArray
    ): String {
        val operation =
            GenerateBitcoinAddressOperation(
                Account(account.getObjectId()),
                backupAddress
            )

        val transaction = configureTransaction(operation, privateKey, ECHO_ASSET_ID)

        return networkBroadcastApiService.broadcastTransactionWithCallback(transaction)
            .dematerialize().toString()
    }

    private fun findAccount(nameOrId: String): Account {
        val accountsMap =
            databaseApiService.getFullAccounts(listOf(nameOrId), false).dematerialize()
        return accountsMap[nameOrId]?.account
            ?: throw AccountNotFoundException("Unable to find required account $nameOrId")
    }

}