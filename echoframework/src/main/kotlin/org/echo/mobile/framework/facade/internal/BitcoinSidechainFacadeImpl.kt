package org.echo.mobile.framework.facade.internal

import com.google.common.primitives.UnsignedLong
import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.ECHO_ASSET_ID
import org.echo.mobile.framework.core.crypto.CryptoCoreComponent
import org.echo.mobile.framework.exception.AccountNotFoundException
import org.echo.mobile.framework.exception.LocalException
import org.echo.mobile.framework.facade.BitcoinSidechainFacade
import org.echo.mobile.framework.model.Account
import org.echo.mobile.framework.model.BtcAddress
import org.echo.mobile.framework.model.operations.GenerateBitcoinAddressOperation
import org.echo.mobile.framework.model.operations.WithdrawBitcoinOperation
import org.echo.mobile.framework.model.socketoperations.TransactionResultCallback
import org.echo.mobile.framework.service.DatabaseApiService
import org.echo.mobile.framework.service.NetworkBroadcastApiService
import org.echo.mobile.framework.support.dematerialize

/**
 * Implementation of [BitcoinSidechainFacade]
 *
 * Wraps logic for sidechain processing
 *
 * @author Dmitriy Bushuev
 */
class BitcoinSidechainFacadeImpl(
        private val databaseApiService: DatabaseApiService,
        private val networkBroadcastApiService: NetworkBroadcastApiService,
        private val cryptoCoreComponent: CryptoCoreComponent,
        private val transactionExpirationDelay: Long
) :
        BaseTransactionsFacade(databaseApiService, cryptoCoreComponent, transactionExpirationDelay),
        BitcoinSidechainFacade {

    override fun generateBitcoinAddress(
            accountNameOrId: String,
            wif: String,
            backupAddress: String,
            broadcastCallback: Callback<Boolean>,
            resultCallback: TransactionResultCallback
    ) {
        try {
            val account = findAccount(accountNameOrId)
            checkOwnerAccount(wif, account)
            val privateKey = cryptoCoreComponent.decodeFromWif(wif)

            val operation =
                    GenerateBitcoinAddressOperation(
                            Account(account.getObjectId()),
                            backupAddress
                    )

            val transaction = configureTransaction(operation, privateKey, ECHO_ASSET_ID)

            val broadCastTransaction = networkBroadcastApiService
                    .broadcastTransaction(transaction)
            broadcastCallback.onSuccess(true)
            resultCallback.processResult(broadCastTransaction)
        } catch (ex: java.lang.Exception) {
            broadcastCallback.onError(ex as? LocalException ?: LocalException(ex))
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
            resultCallback: TransactionResultCallback
    ) {
        try {
            val account = findAccount(accountNameOrId)
            checkOwnerAccount(wif, account)
            val privateKey = cryptoCoreComponent.decodeFromWif(wif)


            val operation =
                    WithdrawBitcoinOperation(
                            Account(account.getObjectId()),
                            btcAddress,
                            UnsignedLong.valueOf(value)
                    )

            val transaction = configureTransaction(operation, privateKey, feeAsset)
            val broadCastTransaction =  networkBroadcastApiService.broadcastTransaction(transaction)
            broadcastCallback.onSuccess(true)
            resultCallback.processResult(broadCastTransaction)
        } catch (ex: java.lang.Exception) {
            broadcastCallback.onError(ex as? LocalException ?: LocalException(ex))
        }
    }

    private fun findAccount(nameOrId: String): Account {
        val accountsMap =
                databaseApiService.getFullAccounts(listOf(nameOrId), false).dematerialize()
        return accountsMap[nameOrId]?.account
                ?: throw AccountNotFoundException("Unable to find required account $nameOrId")
    }

}