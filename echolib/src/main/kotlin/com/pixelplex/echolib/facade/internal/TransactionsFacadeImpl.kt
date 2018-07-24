package com.pixelplex.echolib.facade.internal

import com.google.common.primitives.UnsignedLong
import com.pixelplex.echolib.Callback
import com.pixelplex.echolib.core.crypto.CryptoCoreComponent
import com.pixelplex.echolib.exception.LocalException
import com.pixelplex.echolib.facade.TransactionsFacade
import com.pixelplex.echolib.model.*
import com.pixelplex.echolib.model.network.Network
import com.pixelplex.echolib.model.socketoperations.TransferOperationBuilder
import com.pixelplex.echolib.service.AccountHistoryApiService
import com.pixelplex.echolib.service.DatabaseApiService
import com.pixelplex.echolib.service.NetworkBroadcastApiService
import com.pixelplex.echolib.support.Result
import com.pixelplex.echolib.support.fold
import com.pixelplex.echolib.support.isEqualsByKey

/**
 * Implementation of [TransactionsFacade]
 *
 * <p>
 *     Delegates API call logic to [NetworkBroadcastApiService] and [AccountHistoryApiService]
 * </p>
 *
 * @author Dmitriy Bushuev
 */
class TransactionsFacadeImpl(
    private val databaseApiService: DatabaseApiService,
    private val networkBroadcastApiService: NetworkBroadcastApiService,
    private val cryptoCoreComponent: CryptoCoreComponent,
    private val accountHistoryApiService: AccountHistoryApiService
) : TransactionsFacade {

    override fun sendTransferOperation(
        nameOrId: String,
        password: String,
        toNameOrId: String,
        amount: String,
        asset: String,
        callback: Callback<String>
    ) {
        try {
            val accounts = databaseApiService.getFullAccounts(listOf(nameOrId, toNameOrId), false)

            var toAccount: Account? = null
            var fromAccount: Account? = null

            accounts.fold({ accountsMap ->
                toAccount = accountsMap[toNameOrId]?.account
                fromAccount = accountsMap[nameOrId]?.account
            }, {
                throw LocalException(it.message, it)
            })

            if (toAccount == null || fromAccount == null) {
                throw LocalException("Unable to find required accounts")
            }

            checkOwnerAccount(nameOrId, password, fromAccount!!)

            val transfer = TransferOperationBuilder().setFrom(
                fromAccount!!
            ).setTo(
                toAccount!!
            ).setAmount(
                AssetAmount(
                    UnsignedLong.valueOf(amount.toLong()), Asset(asset)
                )
            ).build()

            val blockData = databaseApiService.getBlockData()
            val chainId = getChainId()
            val privateKey =
                cryptoCoreComponent.getPrivateKey(
                    fromAccount!!.name,
                    password,
                    AuthorityType.OWNER
                )
            val fees = getFees(listOf(transfer), asset)

            val transaction = Transaction(privateKey, blockData, listOf(transfer), chainId)

            transaction.setFees(fees)

            val transactionResult =
                networkBroadcastApiService.broadcastTransactionWithCallback(transaction)

            transactionResult.fold({
                callback.onSuccess(it)
            }, {
                throw (transactionResult as Result.Error).error
            })
        } catch (ex: LocalException) {
            return callback.onError(ex)

        } catch (ex: Exception) {
            return callback.onError(LocalException(ex.message, ex))
        }

    }

    private fun checkOwnerAccount(name: String, password: String, account: Account) {
        val ownerAddress =
            cryptoCoreComponent.getAddress(name, password, AuthorityType.OWNER)

        val isKeySame = account.isEqualsByKey(ownerAddress, AuthorityType.OWNER)
        if (!isKeySame) {
            throw LocalException("Owner account checking exception")
        }
    }

    private fun getChainId(): String {
        val chainIdResult = databaseApiService.getChainId()
        return if (chainIdResult is Result.Value) {
            chainIdResult.value
        } else {
            throw (chainIdResult as Result.Error).error
        }
    }

    private fun getFees(operations: List<BaseOperation>, asset: String): List<AssetAmount> {
        val feesResult =
            databaseApiService.getRequiredFees(operations, Asset(asset))
        return if (feesResult is Result.Value) {
            feesResult.value
        } else {
            throw (feesResult as Result.Error).error
        }
    }

    override fun getAccountHistory(
        nameOrId: String,
        transactionStartId: String,
        transactionStopId: String,
        limit: Int,
        asset: String,
        callback: Callback<HistoryResponse>
    ) {
        accountHistoryApiService.getAccountHistory(
            nameOrId,
            transactionStartId,
            transactionStopId,
            limit,
            callback
        )
    }

}
