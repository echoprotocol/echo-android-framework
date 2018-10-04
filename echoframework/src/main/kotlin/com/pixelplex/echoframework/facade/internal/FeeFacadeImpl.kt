package com.pixelplex.echoframework.facade.internal

import com.google.common.primitives.UnsignedLong
import com.pixelplex.echoframework.Callback
import com.pixelplex.echoframework.core.crypto.CryptoCoreComponent
import com.pixelplex.echoframework.core.logger.internal.LoggerCoreComponent
import com.pixelplex.echoframework.exception.LocalException
import com.pixelplex.echoframework.facade.FeeFacade
import com.pixelplex.echoframework.model.Account
import com.pixelplex.echoframework.model.Asset
import com.pixelplex.echoframework.model.AssetAmount
import com.pixelplex.echoframework.model.Memo
import com.pixelplex.echoframework.model.operations.TransferOperationBuilder
import com.pixelplex.echoframework.processResult
import com.pixelplex.echoframework.service.DatabaseApiService
import com.pixelplex.echoframework.support.Result
import com.pixelplex.echoframework.support.error
import com.pixelplex.echoframework.support.map
import com.pixelplex.echoframework.support.value

/**
 * Implementation of [FeeFacade]
 *
 * Delegates API call logic to [DatabaseApiService]
 *
 * @author Dmitriy Bushuev
 */
class FeeFacadeImpl(
    private val databaseApiService: DatabaseApiService,
    cryptoCoreComponent: CryptoCoreComponent
) : BaseTransactionsFacade(databaseApiService, cryptoCoreComponent),
    FeeFacade {

    override fun getFeeForTransferOperation(
        fromNameOrId: String,
        password: String,
        toNameOrId: String,
        amount: String,
        asset: String,
        feeAsset: String?,
        message: String?,
        callback: Callback<String>
    ) = callback.processResult(Result {
        var toAccount: Account? = null
        var fromAccount: Account? = null

        databaseApiService.getFullAccounts(listOf(fromNameOrId, toNameOrId), false)
            .value { accountsMap ->
                toAccount = accountsMap[toNameOrId]?.account
                fromAccount = accountsMap[fromNameOrId]?.account
            }
            .error { accountsError ->
                throw LocalException("Error occurred during accounts request", accountsError)
            }

        if (toAccount == null || fromAccount == null) {
            LOGGER.log(
                """Unable to find accounts for transfer.
                    |Source = $fromNameOrId
                    |Target = $toNameOrId
                """.trimMargin()
            )
            throw LocalException("Unable to find required accounts: source = $fromNameOrId, target = $toNameOrId")
        }

        val memoPrivateKey = memoKey(fromAccount!!.name, password)
        val memo = generateMemo(memoPrivateKey, fromAccount!!, toAccount!!, message)

        val transfer = buildTransaction(fromAccount!!, toAccount!!, amount, asset, memo)

        getFees(listOf(transfer), feeAsset ?: asset)
    }.map { fees ->
        if (fees.isEmpty()) {
            LOGGER.log(
                """Empty fee list for required operation.
                            |Source = $fromNameOrId
                            |Target = $toNameOrId
                            |Amount = $amount
                            |Asset = $asset
                            |Fee asset = $feeAsset
                        """
            )
            throw LocalException("Unable to get fee for specified operation")
        }

        fees.first().amount.toString()
    })

    override fun getFeeForTransferOperation(
        fromNameOrId: String,
        toNameOrId: String,
        amount: String,
        asset: String,
        feeAsset: String?,
        callback: Callback<String>
    ) = callback.processResult(Result {
        var toAccount: Account? = null
        var fromAccount: Account? = null

        databaseApiService.getFullAccounts(listOf(fromNameOrId, toNameOrId), false)
            .value { accountsMap ->
                toAccount = accountsMap[toNameOrId]?.account
                fromAccount = accountsMap[fromNameOrId]?.account
            }
            .error { accountsError ->
                throw LocalException("Error occurred during accounts request", accountsError)
            }

        if (toAccount == null || fromAccount == null) {
            LOGGER.log(
                """Unable to find accounts for transfer.
                    |Source = $fromNameOrId
                    |Target = $toNameOrId
                """.trimMargin()
            )
            throw LocalException("Unable to find required accounts: source = $fromNameOrId, target = $toNameOrId")
        }

        val transfer = buildTransaction(fromAccount!!, toAccount!!, amount, asset, null)

        getFees(listOf(transfer), feeAsset ?: asset)
    }.map { fees ->
        if (fees.isEmpty()) {
            LOGGER.log(
                """Empty fee list for required operation.
                            |Source = $fromNameOrId
                            |Target = $toNameOrId
                            |Amount = $amount
                            |Asset = $asset
                            |Fee asset = $feeAsset
                        """
            )
            throw LocalException("Unable to get fee for specified operation")
        }

        fees.first().amount.toString()
    })

    private fun buildTransaction(
        fromAccount: Account,
        toAccount: Account,
        amount: String,
        asset: String,
        memo: Memo?
    ) = TransferOperationBuilder()
        .setFrom(fromAccount)
        .setTo(toAccount)
        .setAmount(AssetAmount(UnsignedLong.valueOf(amount.toLong()), Asset(asset)))
        .setMemo(memo)
        .build()

    companion object {
        private val LOGGER = LoggerCoreComponent.create(FeeFacadeImpl::class.java.name)
    }

}
