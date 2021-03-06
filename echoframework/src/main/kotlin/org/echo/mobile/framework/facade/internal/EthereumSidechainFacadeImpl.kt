package org.echo.mobile.framework.facade.internal

import com.google.common.primitives.UnsignedLong
import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.ECHO_ASSET_ID
import org.echo.mobile.framework.core.crypto.CryptoCoreComponent
import org.echo.mobile.framework.exception.AccountNotFoundException
import org.echo.mobile.framework.exception.LocalException
import org.echo.mobile.framework.exception.NotFoundException
import org.echo.mobile.framework.facade.EthereumSidechainFacade
import org.echo.mobile.framework.model.Account
import org.echo.mobile.framework.model.BaseResult
import org.echo.mobile.framework.model.EthAddress
import org.echo.mobile.framework.model.TransactionResult
import org.echo.mobile.framework.model.operations.GenerateEthereumAddressOperation
import org.echo.mobile.framework.model.operations.WithdrawEthereumOperation
import org.echo.mobile.framework.model.socketoperations.ResultCallback
import org.echo.mobile.framework.service.DatabaseApiService
import org.echo.mobile.framework.service.NetworkBroadcastApiService
import org.echo.mobile.framework.support.EthAddressValidator
import org.echo.mobile.framework.support.concurrent.future.FutureTask
import org.echo.mobile.framework.support.concurrent.future.completeCallback
import org.echo.mobile.framework.support.dematerialize

/**
 * Implementation of [EthereumSidechainFacade]
 *
 * Wraps logic for eth type sidechain processing
 *
 * @author Dmitriy Bushuev
 */
class EthereumSidechainFacadeImpl(
        private val databaseApiService: DatabaseApiService,
        private val networkBroadcastApiService: NetworkBroadcastApiService,
        private val cryptoCoreComponent: CryptoCoreComponent,
        private val notifiedTransactionsHelper: NotificationsHelper<TransactionResult>,
        private val transactionExpirationDelay: Long
) :
        BaseTransactionsFacade(databaseApiService, cryptoCoreComponent, transactionExpirationDelay), EthereumSidechainFacade {

    override fun generateEthereumAddress(
            accountNameOrId: String,
            wif: String,
            broadcastCallback: Callback<Boolean>,
            resultCallback: ResultCallback<TransactionResult>
    ) {
        val callId: String
        try {
            val account = findAccount(accountNameOrId)
            checkOwnerAccount(wif, account)
            val privateKey = cryptoCoreComponent.decodeFromWif(wif)

            callId = generateAddress(account, privateKey)

            broadcastCallback.onSuccess(true)
        } catch (exception: Exception) {
            broadcastCallback.onError(exception as? LocalException ?: LocalException(exception))
            return
        }

        retrieveTransactionResult(callId, resultCallback.get())
    }

    override fun ethWithdraw(
            accountNameOrId: String,
            wif: String,
            ethAddress: String,
            value: String,
            feeAsset: String,
            broadcastCallback: Callback<Boolean>,
            resultCallback: ResultCallback<TransactionResult>
    ) {
        val callId: String
        try {
            val account = findAccount(accountNameOrId)
            checkOwnerAccount(wif, account)
            val privateKey = cryptoCoreComponent.decodeFromWif(wif)

            callId = withdraw(ethAddress, account, privateKey, value, feeAsset)

            broadcastCallback.onSuccess(true)
        } catch (exception: Exception) {
            broadcastCallback.onError(exception as? LocalException ?: LocalException(exception))
            return
        }

        retrieveTransactionResult(callId, resultCallback.get())
    }

    private fun generateAddress(account: Account, privateKey: ByteArray): String {
        val operation = GenerateEthereumAddressOperation(Account(account.getObjectId()))

        val transaction = configureTransaction(operation, privateKey, ECHO_ASSET_ID)

        return networkBroadcastApiService.broadcastTransactionWithCallback(transaction)
                .dematerialize().toString()
    }

    private fun withdraw(
            ethAddress: String,
            account: Account,
            privateKey: ByteArray,
            value: String,
            feeAsset: String
    ): String {
        val processedAddress =
                ethAddress.replace(EthAddressValidator.ADDRESS_PREFIX, "").toLowerCase()

        val operation =
                WithdrawEthereumOperation(
                        Account(account.getObjectId()),
                        processedAddress,
                        UnsignedLong.valueOf(value)
                )

        val transaction = configureTransaction(operation, privateKey, feeAsset)

        return networkBroadcastApiService.broadcastTransactionWithCallback(transaction)
                .dematerialize().toString()
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

    override fun getEthereumAddress(
            accountNameOrId: String,
            callback: Callback<EthAddress>
    ) {
        val id = findAccount(accountNameOrId).getObjectId()
        databaseApiService.getEthereumAddress(id, callback)
    }

    private fun findAccount(nameOrId: String): Account {
        val accountsMap =
                databaseApiService.getFullAccounts(listOf(nameOrId), false).dematerialize()
        return accountsMap[nameOrId]?.account
                ?: throw AccountNotFoundException("Unable to find required account $nameOrId")
    }

}