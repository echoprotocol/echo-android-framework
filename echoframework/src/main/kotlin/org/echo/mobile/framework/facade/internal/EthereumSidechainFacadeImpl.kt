package org.echo.mobile.framework.facade.internal

import com.google.common.primitives.UnsignedLong
import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.ECHO_ASSET_ID
import org.echo.mobile.framework.core.crypto.CryptoCoreComponent
import org.echo.mobile.framework.exception.AccountNotFoundException
import org.echo.mobile.framework.exception.LocalException
import org.echo.mobile.framework.facade.EthereumSidechainFacade
import org.echo.mobile.framework.model.Account
import org.echo.mobile.framework.model.EthAddress
import org.echo.mobile.framework.model.operations.GenerateEthereumAddressOperation
import org.echo.mobile.framework.model.operations.WithdrawEthereumOperation
import org.echo.mobile.framework.model.socketoperations.TransactionResultCallback
import org.echo.mobile.framework.service.DatabaseApiService
import org.echo.mobile.framework.service.NetworkBroadcastApiService
import org.echo.mobile.framework.support.EthAddressValidator
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
    private val transactionExpirationDelay: Long
) :
    BaseTransactionsFacade(databaseApiService, cryptoCoreComponent, transactionExpirationDelay), EthereumSidechainFacade {

    override fun generateEthereumAddress(
        accountNameOrId: String,
        wif: String,
        broadcastCallback: Callback<Boolean>,
        resultCallback: TransactionResultCallback
    ) {
        try {
            val account = findAccount(accountNameOrId)
            checkOwnerAccount(wif, account)
            val privateKey = cryptoCoreComponent.decodeFromWif(wif)

            val operation = GenerateEthereumAddressOperation(Account(account.getObjectId()))

            val transaction = configureTransaction(operation, privateKey, ECHO_ASSET_ID)
            val broadCastTransaction = networkBroadcastApiService
                    .broadcastTransaction(transaction)

            broadcastCallback.onSuccess(true)
            resultCallback.processResult(broadCastTransaction)
        } catch (ex: java.lang.Exception) {
            broadcastCallback.onError(ex as? LocalException ?: LocalException(ex))
        }
    }

    override fun ethWithdraw(
        accountNameOrId: String,
        wif: String,
        ethAddress: String,
        value: String,
        feeAsset: String,
        broadcastCallback: Callback<Boolean>,
        resultCallback: TransactionResultCallback
    ) {
        try {
            val account = findAccount(accountNameOrId)
            checkOwnerAccount(wif, account)
            val privateKey = cryptoCoreComponent.decodeFromWif(wif)

            val processedAddress =
                    ethAddress.replace(EthAddressValidator.ADDRESS_PREFIX, "").toLowerCase()

            val operation =
                    WithdrawEthereumOperation(
                            Account(account.getObjectId()),
                            processedAddress,
                            UnsignedLong.valueOf(value)
                    )

            val transaction = configureTransaction(operation, privateKey, feeAsset)

            val broadCastTransaction = networkBroadcastApiService
                    .broadcastTransaction(transaction)

            broadcastCallback.onSuccess(true)
            resultCallback.processResult(broadCastTransaction)
        } catch (ex: java.lang.Exception) {
            broadcastCallback.onError(ex as? LocalException ?: LocalException(ex))
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