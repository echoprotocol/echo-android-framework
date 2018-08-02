package com.pixelplex.echoframework.facade.internal

import com.google.common.primitives.UnsignedLong
import com.pixelplex.echoframework.Callback
import com.pixelplex.echoframework.core.logger.internal.LoggerCoreComponent
import com.pixelplex.echoframework.exception.LocalException
import com.pixelplex.echoframework.facade.FeeFacade
import com.pixelplex.echoframework.model.Account
import com.pixelplex.echoframework.model.Asset
import com.pixelplex.echoframework.model.AssetAmount
import com.pixelplex.echoframework.model.operations.TransferOperationBuilder
import com.pixelplex.echoframework.service.DatabaseApiService
import com.pixelplex.echoframework.support.error
import com.pixelplex.echoframework.support.value

/**
 * Implementation of [FeeFacade]
 *
 * Delegates API call logic to [DatabaseApiService]
 *
 * @author Dmitriy Bushuev
 */
class FeeFacadeImpl(private val databaseApiService: DatabaseApiService) : FeeFacade {

    override fun getFeeForTransferOperation(
        fromNameOrId: String,
        toNameOrId: String,
        amount: String,
        asset: String,
        callback: Callback<String>
    ) {
        try {
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

            val transfer = TransferOperationBuilder().setFrom(
                fromAccount!!
            ).setTo(
                toAccount!!
            ).setAmount(
                AssetAmount(
                    UnsignedLong.valueOf(amount.toLong()), Asset(asset)
                )
            ).build()

            databaseApiService.getRequiredFees(listOf(transfer), Asset(asset))
                .value { fees ->
                    if (fees.isEmpty()) {
                        LOGGER.log(
                            """Empty fee list for required operation.
                            |Source = $fromNameOrId
                            |Target = $toNameOrId
                            |Amount = $amount
                            |Asset = $asset
                        """
                        )
                        callback.onError(LocalException("Unable to get fee for specified operation"))
                        return
                    }

                    callback.onSuccess(fees[0].amount.toString())
                }
                .error { error ->
                    callback.onError(LocalException(error.message, error))
                }

        } catch (ex: LocalException) {
            return callback.onError(ex)
        } catch (ex: Exception) {
            return callback.onError(LocalException(ex.message, ex))
        }
    }

    companion object {
        private val LOGGER = LoggerCoreComponent.create(FeeFacadeImpl::class.java.name)
    }

}
