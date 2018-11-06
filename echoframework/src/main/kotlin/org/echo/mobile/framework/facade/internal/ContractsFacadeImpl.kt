package org.echo.mobile.framework.facade.internal

import com.google.common.primitives.UnsignedLong
import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.core.crypto.CryptoCoreComponent
import org.echo.mobile.framework.core.socket.SocketCoreComponent
import org.echo.mobile.framework.exception.LocalException
import org.echo.mobile.framework.facade.ContractsFacade
import org.echo.mobile.framework.model.*
import org.echo.mobile.framework.model.contract.ContractInfo
import org.echo.mobile.framework.model.contract.ContractResult
import org.echo.mobile.framework.model.contract.ContractStruct
import org.echo.mobile.framework.model.contract.input.ContractInputEncoder
import org.echo.mobile.framework.model.contract.input.InputValue
import org.echo.mobile.framework.model.network.Network
import org.echo.mobile.framework.model.operations.ContractOperationBuilder
import org.echo.mobile.framework.processResult
import org.echo.mobile.framework.service.DatabaseApiService
import org.echo.mobile.framework.service.NetworkBroadcastApiService
import org.echo.mobile.framework.support.concurrent.future.FutureTask
import org.echo.mobile.framework.support.concurrent.future.completeCallback
import org.echo.mobile.framework.support.dematerialize
import org.echo.mobile.framework.support.error
import org.echo.mobile.framework.support.value

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
    socketCoreComponent: SocketCoreComponent,
    network: Network
) : BaseNotifiedTransactionsFacade(
    databaseApiService,
    cryptoCoreComponent,
    socketCoreComponent,
    network
), ContractsFacade {

    override fun createContract(
        registrarNameOrId: String,
        password: String,
        assetId: String,
        feeAsset: String?,
        byteCode: String,
        params: List<InputValue>,
        gasLimit: Long,
        gasPrice: Long,
        callback: Callback<String>
    ) = callback.processResult {

        var registrar: Account? = null

        databaseApiService.getFullAccounts(listOf(registrarNameOrId), false)
            .value { accountsMap ->
                registrar = accountsMap[registrarNameOrId]?.account
                        ?:
                        throw LocalException("Unable to find required account $registrarNameOrId")
            }
            .error { accountsError ->
                throw LocalException("Error occurred during accounts request", accountsError)
            }

        checkOwnerAccount(registrar!!.name, password, registrar!!)

        val privateKey = cryptoCoreComponent.getPrivateKey(
            registrar!!.name,
            password,
            AuthorityType.ACTIVE
        )

        val constructorParams = ContractInputEncoder().encode("", params)

        val contractOperation = ContractOperationBuilder()
            .setAsset(assetId)
            .setRegistrar(registrar!!)
            .setGas(gasLimit)
            .setGasPrice(gasPrice)
            .setContractCode(byteCode + constructorParams)
            .build()

        val blockData = databaseApiService.getBlockData()
        val chainId = getChainId()
        val fees = getFees(listOf(contractOperation), feeAsset ?: assetId)

        val transaction = Transaction(blockData, listOf(contractOperation), chainId).apply {
            setFees(fees)
            addPrivateKey(privateKey)
        }

        val callId =
            networkBroadcastApiService.broadcastTransactionWithCallback(transaction)
                .dematerialize()

        val future = FutureTask<TransactionResult>()
        subscribeOnTransactionResult(callId.toString(), future.completeCallback())

        future.get()?.trx?.operationsWithResults?.values?.firstOrNull()
            ?: throw LocalException("Result of contract creation not found.")
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
        gasLimit: Long,
        gasPrice: Long,
        callback: Callback<String>
    ) = callback.processResult {

        var registrar: Account? = null

        databaseApiService.getFullAccounts(listOf(userNameOrId), false)
            .value { accountsMap ->
                registrar = accountsMap[userNameOrId]?.account
                        ?: throw LocalException("Unable to find required account $userNameOrId")
            }
            .error { accountsError ->
                throw LocalException("Error occurred during accounts request", accountsError)
            }

        checkOwnerAccount(registrar!!.name, password, registrar!!)

        val privateKey = cryptoCoreComponent.getPrivateKey(
            registrar!!.name,
            password,
            AuthorityType.ACTIVE
        )

        val contractCode = ContractInputEncoder().encode(methodName, methodParams)

        val contractOperation = ContractOperationBuilder()
            .setAsset(assetId)
            .setRegistrar(registrar!!)
            .setGas(gasLimit)
            .setGasPrice(gasPrice)
            .setReceiver(contractId)
            .setContractCode(contractCode)
            .setValue(UnsignedLong.valueOf(value))
            .build()

        val blockData = databaseApiService.getBlockData()
        val chainId = getChainId()
        val fees = getFees(listOf(contractOperation), feeAsset ?: assetId)

        val transaction = Transaction(blockData, listOf(contractOperation), chainId)
            .apply {
                setFees(fees)
                addPrivateKey(privateKey)
            }

        val callId = networkBroadcastApiService.broadcastTransactionWithCallback(transaction)
            .dematerialize()

        val future = FutureTask<TransactionResult>()
        subscribeOnTransactionResult(callId.toString(), future.completeCallback())

        future.get()?.trx?.operationsWithResults?.values?.firstOrNull() ?: ""
    }

    override fun queryContract(
        userNameOrId: String,
        assetId: String,
        contractId: String,
        methodName: String,
        methodParams: List<InputValue>,
        callback: Callback<String>
    ) = callback.processResult {
        var registrar: Account? = null

        databaseApiService.getFullAccounts(listOf(userNameOrId), false)
            .value { accountsMap ->
                registrar = accountsMap[userNameOrId]?.account
                        ?: throw LocalException("Unable to find required account $userNameOrId")
            }
            .error { accountsError ->
                throw LocalException("Error occurred during accounts request", accountsError)
            }

        val contractCode = ContractInputEncoder().encode(methodName, methodParams)

        databaseApiService.callContractNoChangingState(
            contractId,
            registrar!!.getObjectId(),
            assetId,
            contractCode
        ).dematerialize()
    }

    override fun getContractResult(historyId: String, callback: Callback<ContractResult>) =
        callback.processResult(databaseApiService.getContractResult(historyId))

    override fun getContractLogs(
        contractId: String, fromBlock: String, toBlock: String, callback: Callback<List<Log>>
    ) = callback.processResult(
        databaseApiService.getContractLogs(
            contractId,
            fromBlock,
            toBlock
        )
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

}
