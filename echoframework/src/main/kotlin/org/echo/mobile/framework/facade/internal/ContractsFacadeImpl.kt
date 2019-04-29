package org.echo.mobile.framework.facade.internal

import com.google.common.primitives.UnsignedLong
import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.core.crypto.CryptoCoreComponent
import org.echo.mobile.framework.exception.AccountNotFoundException
import org.echo.mobile.framework.exception.LocalException
import org.echo.mobile.framework.exception.NotFoundException
import org.echo.mobile.framework.facade.ContractsFacade
import org.echo.mobile.framework.model.Account
import org.echo.mobile.framework.model.Asset
import org.echo.mobile.framework.model.AssetAmount
import org.echo.mobile.framework.model.AuthorityType
import org.echo.mobile.framework.model.BaseOperation
import org.echo.mobile.framework.model.Log
import org.echo.mobile.framework.model.Transaction
import org.echo.mobile.framework.model.TransactionResult
import org.echo.mobile.framework.model.contract.ContractInfo
import org.echo.mobile.framework.model.contract.ContractResult
import org.echo.mobile.framework.model.contract.ContractStruct
import org.echo.mobile.framework.model.contract.input.ContractInputEncoder
import org.echo.mobile.framework.model.contract.input.InputValue
import org.echo.mobile.framework.model.operations.ContractCallOperationBuilder
import org.echo.mobile.framework.model.operations.ContractCreateOperationBuilder
import org.echo.mobile.framework.processResult
import org.echo.mobile.framework.service.DatabaseApiService
import org.echo.mobile.framework.service.NetworkBroadcastApiService
import org.echo.mobile.framework.support.Provider
import org.echo.mobile.framework.support.concurrent.future.FutureTask
import org.echo.mobile.framework.support.concurrent.future.completeCallback
import org.echo.mobile.framework.support.dematerialize
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Implementation of [ContractsFacade]
 *
 * Delegates API call logic to [DatabaseApiService] and [NetworkBroadcastApiService]
 *
 * @author Daria Pechkovskaya
 */
class ContractsFacadeImpl(
    private val databaseApiService: DatabaseApiService,
    private val networkBroadcastApiService: NetworkBroadcastApiService,
    private val cryptoCoreComponent: CryptoCoreComponent,
    private val notifiedTransactionsHelper: NotifiedTransactionsHelper,
    private val feeRatioProvider: Provider<Double>
) : BaseTransactionsFacade(
    databaseApiService,
    cryptoCoreComponent
), ContractsFacade {

    override fun createContract(
        registrarNameOrId: String,
        password: String,
        value: String,
        assetId: String,
        feeAsset: String?,
        byteCode: String,
        params: List<InputValue>,
        broadcastCallback: Callback<Boolean>,
        resultCallback: Callback<String>?
    ) {
        val callId: String
        try {
            val registrar = findRegistrar(registrarNameOrId)

            checkOwnerAccount(registrar.name, password, registrar)

            val privateKey = cryptoCoreComponent.getPrivateKey(
                registrar.name,
                password,
                AuthorityType.ACTIVE
            )

            callId = createContract(
                registrar,
                privateKey,
                value,
                assetId,
                feeAsset,
                byteCode,
                params
            )

            broadcastCallback.onSuccess(true)
        } catch (ex: Exception) {
            broadcastCallback.onError(ex as? LocalException ?: LocalException(ex))
            return
        }

        resultCallback?.let {
            retrieveTransactionResult(callId, it)
        }
    }

    override fun createContractWithWif(
        registrarNameOrId: String,
        wif: String,
        value: String,
        assetId: String,
        feeAsset: String?,
        byteCode: String,
        params: List<InputValue>,
        broadcastCallback: Callback<Boolean>,
        resultCallback: Callback<String>?
    ) {
        val callId: String
        try {
            val registrar = findRegistrar(registrarNameOrId)

            checkOwnerAccount(wif, registrar)

            val privateKey = cryptoCoreComponent.decodeFromWif(wif)

            callId = createContract(
                registrar,
                privateKey,
                value,
                assetId,
                feeAsset,
                byteCode,
                params
            )

            broadcastCallback.onSuccess(true)
        } catch (ex: Exception) {
            broadcastCallback.onError(ex as? LocalException ?: LocalException(ex))
            return
        }

        resultCallback?.let {
            retrieveTransactionResult(callId, it)
        }
    }

    override fun callContract(
        userNameOrId: String,
        password: String,
        assetId: String,
        feeAsset: String?,
        contractId: String,
        methodName: String,
        methodParams: List<InputValue>,
        value: String,
        broadcastCallback: Callback<Boolean>,
        resultCallback: Callback<String>?
    ) {
        val contractCode = try {
            ContractInputEncoder().encode(methodName, methodParams)
        } catch (exception: Exception) {
            broadcastCallback.onError(exception as? LocalException ?: LocalException(exception))
            return
        }

        callContract(
            userNameOrId,
            password,
            assetId,
            feeAsset,
            contractId,
            contractCode,
            value,
            broadcastCallback,
            resultCallback
        )
    }

    override fun callContract(
        userNameOrId: String,
        password: String,
        assetId: String,
        feeAsset: String?,
        contractId: String,
        code: String,
        value: String,
        broadcastCallback: Callback<Boolean>,
        resultCallback: Callback<String>?
    ) {
        val callId: String
        try {
            val registrar = findRegistrar(userNameOrId)

            checkOwnerAccount(registrar.name, password, registrar)

            val privateKey = cryptoCoreComponent.getPrivateKey(
                registrar.name,
                password,
                AuthorityType.ACTIVE
            )

            callId = callContract(
                registrar,
                privateKey,
                assetId,
                feeAsset,
                contractId,
                code,
                value
            )

            broadcastCallback.onSuccess(true)
        } catch (ex: Exception) {
            broadcastCallback.onError(ex as? LocalException ?: LocalException(ex))
            return
        }

        resultCallback?.let {
            retrieveTransactionResult(callId, it, default = "")
        }
    }

    override fun callContractWithWif(
        userNameOrId: String,
        wif: String,
        assetId: String,
        feeAsset: String?,
        contractId: String,
        methodName: String,
        methodParams: List<InputValue>,
        value: String,
        broadcastCallback: Callback<Boolean>,
        resultCallback: Callback<String>?
    ) {
        val contractCode = try {
            ContractInputEncoder().encode(methodName, methodParams)
        } catch (exception: Exception) {
            broadcastCallback.onError(exception as? LocalException ?: LocalException(exception))
            return
        }

        callContractWithWif(
            userNameOrId,
            wif,
            assetId,
            feeAsset,
            contractId,
            contractCode,
            value,
            broadcastCallback,
            resultCallback
        )
    }

    override fun callContractWithWif(
        userNameOrId: String,
        wif: String,
        assetId: String,
        feeAsset: String?,
        contractId: String,
        code: String,
        value: String,
        broadcastCallback: Callback<Boolean>,
        resultCallback: Callback<String>?
    ) {
        val callId: String
        try {
            val registrar = findRegistrar(userNameOrId)

            checkOwnerAccount(wif, registrar)

            val privateKey = cryptoCoreComponent.decodeFromWif(wif)

            callId = callContract(
                registrar,
                privateKey,
                assetId,
                feeAsset,
                contractId,
                code,
                value
            )

            broadcastCallback.onSuccess(true)
        } catch (ex: Exception) {
            broadcastCallback.onError(ex as? LocalException ?: LocalException(ex))
            return
        }

        resultCallback?.let {
            retrieveTransactionResult(callId, it, default = "")
        }
    }

    override fun queryContract(
        userNameOrId: String,
        assetId: String,
        contractId: String,
        methodName: String,
        methodParams: List<InputValue>,
        callback: Callback<String>
    ) = callback.processResult {
        val registrar = findRegistrar(userNameOrId)

        val contractCode = ContractInputEncoder().encode(methodName, methodParams)

        databaseApiService.callContractNoChangingState(
            contractId,
            registrar.getObjectId(),
            assetId,
            contractCode
        ).dematerialize()
    }

    override fun queryContract(
        userNameOrId: String,
        assetId: String,
        contractId: String,
        code: String,
        callback: Callback<String>
    ) = callback.processResult {
        val registrar = findRegistrar(userNameOrId)

        databaseApiService.callContractNoChangingState(
            contractId,
            registrar.getObjectId(),
            assetId,
            code
        ).dematerialize()
    }

    override fun getContractResult(historyId: String, callback: Callback<ContractResult>) =
        callback.processResult(databaseApiService.getContractResult(historyId))

    override fun getContractLogs(
        contractId: String, fromBlock: String, toBlock: String, callback: Callback<List<Log>>
    ) = callback.processResult(
        databaseApiService.getContractLogs(contractId, fromBlock, toBlock)
    )

    override fun getContracts(
        contractIds: List<String>,
        callback: Callback<List<ContractInfo>>
    ) =
        callback.processResult(databaseApiService.getContracts(contractIds))

    override fun getAllContracts(callback: Callback<List<ContractInfo>>) =
        callback.processResult(databaseApiService.getAllContracts())

    override fun getContract(contractId: String, callback: Callback<ContractStruct>) =
        callback.processResult(databaseApiService.getContract(contractId))

    private fun findRegistrar(nameOrId: String): Account {
        val accountsMap =
            databaseApiService.getFullAccounts(listOf(nameOrId), false).dematerialize()
        return accountsMap[nameOrId]?.account
            ?: throw AccountNotFoundException("Unable to find required account $nameOrId")
    }

    private fun createContract(
        registrar: Account,
        privateKey: ByteArray,
        value: String,
        assetId: String,
        feeAsset: String?,
        byteCode: String,
        params: List<InputValue>
    ): String {
        val constructorParams = ContractInputEncoder().encode("", params)

        val contractOperation = ContractCreateOperationBuilder()
            .setAsset(assetId)
            .setValue(UnsignedLong.valueOf(value))
            .setRegistrar(registrar)
            .setContractCode(byteCode + constructorParams)
            .build()

        val transaction = buildTransaction(privateKey, assetId, feeAsset, contractOperation)

        return networkBroadcastApiService.broadcastTransactionWithCallback(transaction)
            .dematerialize().toString()
    }

    private fun callContract(
        registrar: Account,
        privateKey: ByteArray,
        assetId: String,
        feeAsset: String?,
        contractId: String,
        code: String,
        value: String
    ): String {
        val contractOperation = ContractCallOperationBuilder()
            .setRegistrar(registrar)
            .setReceiver(contractId)
            .setContractCode(code)
            .setValue(AssetAmount(UnsignedLong.valueOf(value), Asset(assetId)))
            .build()

        val transaction = buildTransaction(
            privateKey,
            assetId,
            feeAsset,
            contractOperation,
            feeRatioProvider.provide()
        )

        return networkBroadcastApiService.broadcastTransactionWithCallback(transaction)
            .dematerialize().toString()
    }

    private fun buildTransaction(
        privateKey: ByteArray,
        assetId: String,
        feeAsset: String?,
        operation: BaseOperation,
        feeRatio: Double? = null
    ): Transaction {
        val blockData = databaseApiService.getBlockData()
        val chainId = getChainId()
        val rawFees = getFees(listOf(operation), feeAsset ?: assetId)

        val ratioFees = feeRatio?.let { ratio ->
            multiplyFees(rawFees.toMutableList(), ratio)
        } ?: rawFees

        return Transaction(blockData, listOf(operation), chainId).apply {
            setFees(ratioFees)
            addPrivateKey(privateKey)
        }
    }

    private fun multiplyFees(
        rawFees: MutableList<AssetAmount>, feeRatio: Double
    ): List<AssetAmount> =
        rawFees.map { assetAmount ->
            assetAmount.multiplyBy(feeRatio, RoundingMode.FLOOR)
        }

    private fun retrieveTransactionResult(
        callId: String,
        callback: Callback<String>,
        default: String? = null
    ) {
        try {
            val future = FutureTask<TransactionResult>()
            notifiedTransactionsHelper.subscribeOnTransactionResult(
                callId,
                future.completeCallback()
            )

            val result = future.get()?.trx?.operationsWithResults?.values?.firstOrNull()
                ?: default
                ?: throw NotFoundException("Result of operation not found.")

            callback.onSuccess(result)
        } catch (ex: Exception) {
            callback.onError(ex as? LocalException ?: LocalException(ex))
        }
    }

}
