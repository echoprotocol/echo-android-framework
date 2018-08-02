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
import com.pixelplex.echoframework.support.dematerialize
import com.pixelplex.echoframework.support.error
import com.pixelplex.echoframework.support.value
import java.math.BigInteger

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
) : TransactionsFacade {

    override fun sendTransferOperation(
        nameOrId: String,
        password: String,
        toNameOrId: String,
        amount: String,
        asset: String,
        message: String?,
        callback: Callback<Boolean>
    ) {
        try {
            var toAccount: Account? = null
            var fromAccount: Account? = null

            databaseApiService.getFullAccounts(listOf(nameOrId, toNameOrId), false)
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

            val privateKey =
                cryptoCoreComponent.getPrivateKey(
                    fromAccount!!.name,
                    password,
                    AuthorityType.ACTIVE
                )

            val memo = generateMemo(privateKey, fromAccount!!, toAccount!!, message)

            val transfer = TransferOperationBuilder()
                .setFrom(fromAccount!!)
                .setTo(toAccount!!)
                .setAmount(AssetAmount(UnsignedLong.valueOf(amount.toLong()), Asset(asset)))
                .setMemo(memo)
                .build()

            val blockData = databaseApiService.getBlockData()
            val chainId = getChainId()
            val fees = getFees(listOf(transfer), asset)

            val transaction = Transaction(
                privateKey,
                blockData,
                listOf(transfer),
                chainId
            ).apply { setFees(fees) }

            val transactionResult =
                networkBroadcastApiService.broadcastTransactionWithCallback(transaction)

            callback.onSuccess(transactionResult.dematerialize())
        } catch (ex: LocalException) {
            callback.onError(ex)
        } catch (ex: Exception) {
            callback.onError(LocalException(ex.message, ex))
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

    private fun generateMemo(
        privateKey: ByteArray,
        fromAccount: Account,
        toAccount: Account,
        message: String?
    ): Memo {
        if (message != null) {
            val encryptedMessage = cryptoCoreComponent.encryptMessage(
                privateKey,
                toAccount.options.memoKey!!.key,
                BigInteger.ZERO,
                message
            )

            return Memo(
                Address(fromAccount.options.memoKey!!),
                Address(toAccount.options.memoKey!!),
                BigInteger.ZERO,
                encryptedMessage ?: ByteArray(0)
            )
        }

        return Memo()
    }

    private fun getChainId(): String = databaseApiService.getChainId().dematerialize()

    private fun getFees(operations: List<BaseOperation>, asset: String): List<AssetAmount> =
        databaseApiService.getRequiredFees(operations, Asset(asset)).dematerialize()

}
