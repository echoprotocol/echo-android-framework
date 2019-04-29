package org.echo.mobile.framework.facade.internal

import com.google.common.primitives.UnsignedLong
import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.core.crypto.CryptoCoreComponent
import org.echo.mobile.framework.core.logger.internal.LoggerCoreComponent
import org.echo.mobile.framework.exception.AccountNotFoundException
import org.echo.mobile.framework.exception.LocalException
import org.echo.mobile.framework.facade.FeeFacade
import org.echo.mobile.framework.facade.TransactionFacadeExtension
import org.echo.mobile.framework.model.Account
import org.echo.mobile.framework.model.Asset
import org.echo.mobile.framework.model.AssetAmount
import org.echo.mobile.framework.model.Memo
import org.echo.mobile.framework.model.contract.input.ContractInputEncoder
import org.echo.mobile.framework.model.contract.input.InputValue
import org.echo.mobile.framework.model.operations.ContractCallOperation
import org.echo.mobile.framework.model.operations.ContractCallOperationBuilder
import org.echo.mobile.framework.model.operations.TransferOperationBuilder
import org.echo.mobile.framework.processResult
import org.echo.mobile.framework.service.DatabaseApiService
import org.echo.mobile.framework.support.Provider
import org.echo.mobile.framework.support.Result
import org.echo.mobile.framework.support.error
import org.echo.mobile.framework.support.map
import org.echo.mobile.framework.support.value
import java.math.RoundingMode

/**
 * Implementation of [FeeFacade]
 *
 * Delegates API call logic to [DatabaseApiService]
 *
 * @author Dmitriy Bushuev
 */
class FeeFacadeImpl(
    private val feeRatioProvider: Provider<Double>,
    override val databaseApiService: DatabaseApiService,
    override val cryptoCoreComponent: CryptoCoreComponent
) : FeeFacade, TransactionFacadeExtension {

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
        val (fromAccount, toAccount) = getParticipantsPair(fromNameOrId, toNameOrId)

        val memoPrivateKey = memoKey(fromAccount.name, password)
        val memo = generateMemo(memoPrivateKey, fromAccount, toAccount, message)

        val transfer = buildTransaction(fromAccount, toAccount, amount, asset, memo)

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

    override fun getFeeForTransferOperationWithWif(
        fromNameOrId: String,
        wif: String,
        toNameOrId: String,
        amount: String,
        asset: String,
        feeAsset: String?,
        message: String?,
        callback: Callback<String>
    ) = callback.processResult(Result {
        val (fromAccount, toAccount) = getParticipantsPair(fromNameOrId, toNameOrId)

        val memoPrivateKey = cryptoCoreComponent.decodeFromWif(wif)
        val memo = generateMemo(memoPrivateKey, fromAccount, toAccount, message)

        val transfer = buildTransaction(fromAccount, toAccount, amount, asset, memo)

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

    override fun getFeeForContractOperation(
        userNameOrId: String,
        contractId: String,
        amount: String,
        methodName: String,
        methodParams: List<InputValue>,
        assetId: String,
        feeAsset: String?,
        callback: Callback<String>
    ) = callback.processResult(Result {
        val contractCode = ContractInputEncoder().encode(methodName, methodParams)

        val contractOperation =
            configureContractTransaction(userNameOrId, contractId, amount, contractCode, assetId)

        getFees(listOf(contractOperation), feeAsset ?: assetId)

    }.map { fees ->
        if (fees.isEmpty()) {
            LOGGER.log(
                """Empty fee list for required operation.
                            |Caller = $userNameOrId
                            |Target = $contractId
                            |Method name = $methodName
                            |Method params = ${methodParams.joinToString()}
                            |Fee asset = ${feeAsset ?: assetId}
                        """
            )
            throw LocalException("Unable to get fee for specified operation")
        }

        multiplyFee(fees.first(), feeRatioProvider.provide()).toString()
    })

    override fun getFeeForContractOperation(
        userNameOrId: String,
        contractId: String,
        amount: String,
        code: String,
        assetId: String,
        feeAsset: String?,
        callback: Callback<String>
    ) = callback.processResult(Result {
        val contractOperation =
            configureContractTransaction(userNameOrId, contractId, amount, code, assetId)

        getFees(listOf(contractOperation), feeAsset ?: assetId)

    }.map { fees ->
        if (fees.isEmpty()) {
            LOGGER.log(
                """Empty fee list for required operation.
                            |Caller = $userNameOrId
                            |Target = $contractId
                            |Code = $code
                            |Fee asset = ${feeAsset ?: assetId}
                        """
            )
            throw LocalException("Unable to get fee for specified operation")
        }

        multiplyFee(fees.first(), feeRatioProvider.provide()).toString()
    })

    private fun multiplyFee(rawFee: AssetAmount, feeRatio: Double): AssetAmount =
        rawFee.multiplyBy(feeRatio, RoundingMode.FLOOR)

    private fun buildTransaction(
        fromAccount: Account,
        toAccount: Account,
        amount: String,
        asset: String,
        memo: Memo
    ) = TransferOperationBuilder()
        .setFrom(fromAccount)
        .setTo(toAccount)
        .setAmount(AssetAmount(UnsignedLong.valueOf(amount.toLong()), Asset(asset)))
        .setMemo(memo)
        .build()

    private fun configureContractTransaction(
        userNameOrId: String,
        contractId: String,
        amount: String,
        code: String,
        assetId: String
    ): ContractCallOperation {
        var account: Account? = null

        databaseApiService.getFullAccounts(listOf(userNameOrId), false)
            .value { accountsMap ->
                account = accountsMap[userNameOrId]?.account
            }
            .error { accountsError ->
                throw LocalException("Error occurred during accounts request", accountsError)
            }

        if (account == null) {
            LOGGER.log(
                """Unable to find accounts for contract call.
                    |Caller = $account
                """.trimMargin()
            )
            throw AccountNotFoundException("Unable to find required accounts: caller = $account")
        }

        return ContractCallOperationBuilder()
            .setFee(AssetAmount(UnsignedLong.ZERO, Asset(assetId)))
            .setRegistrar(account!!)
            .setReceiver(contractId)
            .setContractCode(code)
            .setValue(AssetAmount(UnsignedLong.valueOf(amount), Asset(assetId)))
            .build()
    }

    companion object {
        private val LOGGER = LoggerCoreComponent.create(FeeFacadeImpl::class.java.name)
    }

}
