package com.pixelplex.echoframework.facade.internal

import com.google.common.primitives.UnsignedLong
import com.pixelplex.echoframework.Callback
import com.pixelplex.echoframework.core.crypto.CryptoCoreComponent
import com.pixelplex.echoframework.exception.LocalException
import com.pixelplex.echoframework.facade.TransactionsFacade
import com.pixelplex.echoframework.model.*
import com.pixelplex.echoframework.model.operations.TransferOperationBuilder
import com.pixelplex.echoframework.service.DatabaseApiService
import com.pixelplex.echoframework.service.NetworkBroadcastApiService
import com.pixelplex.echoframework.support.*

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
    private val cryptoCoreComponent: CryptoCoreComponent
) : TransactionsFacade {

    override fun sendTransferOperation(
        nameOrId: String,
        password: String,
        toNameOrId: String,
        amount: String,
        asset: String,
        callback: Callback<Boolean>
    ) {
        try {
            val accounts = databaseApiService.getFullAccounts(listOf(nameOrId, toNameOrId), false)

            var toAccount: Account? = null
            var fromAccount: Account? = null

            accounts
                .value { accountsMap ->
                    toAccount = accountsMap[toNameOrId]?.account
                    fromAccount = accountsMap[nameOrId]?.account
                }
                .error { accountsError ->
                    throw LocalException("Error occurred during accounts request", accountsError)
                }

            if (toAccount == null || fromAccount == null) {
                throw LocalException("Unable to find required accounts: source = $nameOrId, target = $toNameOrId")
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

}
