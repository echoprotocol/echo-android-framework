package org.echo.mobile.framework.facade.internal

import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.core.crypto.CryptoCoreComponent
import org.echo.mobile.framework.exception.LocalException
import org.echo.mobile.framework.facade.ContractsFacade
import org.echo.mobile.framework.model.Account
import org.echo.mobile.framework.model.AuthorityType
import org.echo.mobile.framework.model.Transaction
import org.echo.mobile.framework.model.contract.*
import org.echo.mobile.framework.model.operations.ContractOperationBuilder
import org.echo.mobile.framework.processResult
import org.echo.mobile.framework.service.DatabaseApiService
import org.echo.mobile.framework.service.NetworkBroadcastApiService
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
    private val cryptoCoreComponent: CryptoCoreComponent
) : BaseTransactionsFacade(databaseApiService, cryptoCoreComponent), ContractsFacade {

    override fun createContract(
        registrarNameOrId: String,
        password: String,
        assetId: String,
        byteCode: String,
        callback: Callback<Boolean>
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

        checkOwnerAccount(registrarNameOrId, password, registrar!!)

        val privateKey = cryptoCoreComponent.getPrivateKey(
            registrar!!.name,
            password,
            AuthorityType.ACTIVE
        )

        val contractOperation = ContractOperationBuilder()
            .setAsset(assetId)
            .setRegistrar(registrar!!)
            .setGas(1000000)
            .setContractCode(byteCode)
            .build()

        val blockData = databaseApiService.getBlockData()
        val chainId = getChainId()
        val fees = getFees(listOf(contractOperation), assetId)

        val transaction = Transaction(blockData, listOf(contractOperation), chainId)
            .apply {
                setFees(fees)
                addPrivateKey(privateKey)
            }

        networkBroadcastApiService.broadcastTransactionWithCallback(transaction).dematerialize()
    }

    override fun callContract(
        registrarNameOrId: String,
        password: String,
        assetId: String,
        contractId: String,
        methodName: String,
        methodParams: List<ContractMethodParameter>,
        callback: Callback<Boolean>
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

        checkOwnerAccount(registrarNameOrId, password, registrar!!)

        val privateKey = cryptoCoreComponent.getPrivateKey(
            registrar!!.name,
            password,
            AuthorityType.ACTIVE
        )

        val contractCode = ContractCodeBuilder()
            .setMethodName(methodName)
            .setMethodParams(methodParams)
            .build()

        val contractOperation = ContractOperationBuilder()
            .setAsset(assetId)
            .setRegistrar(registrar!!)
            .setGas(1000000)
            .setReceiver(contractId)
            .setContractCode(contractCode)
            .build()

        val blockData = databaseApiService.getBlockData()
        val chainId = getChainId()
        val fees = getFees(listOf(contractOperation), assetId)

        val transaction = Transaction(blockData, listOf(contractOperation), chainId)
            .apply {
                setFees(fees)
                addPrivateKey(privateKey)
            }

        networkBroadcastApiService.broadcastTransactionWithCallback(transaction).dematerialize()
    }

    override fun queryContract(
        registrarNameOrId: String,
        assetId: String,
        contractId: String,
        methodName: String,
        methodParams: List<ContractMethodParameter>,
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

        val contractCode = ContractCodeBuilder()
            .setMethodName(methodName)
            .setMethodParams(methodParams)
            .build()

        databaseApiService.callContractNoChangingState(
            contractId,
            registrar!!.getObjectId(),
            assetId,
            contractCode
        ).dematerialize()
    }

    override fun getContractResult(historyId: String, callback: Callback<ContractResult>) =
        callback.processResult(databaseApiService.getContractResult(historyId))

    override fun getContracts(contractIds: List<String>, callback: Callback<List<ContractInfo>>) =
        callback.processResult(databaseApiService.getContracts(contractIds))

    override fun getAllContracts(callback: Callback<List<ContractInfo>>) =
        callback.processResult(databaseApiService.getAllContracts())

    override fun getContract(contractId: String, callback: Callback<ContractStruct>) =
        callback.processResult(databaseApiService.getContract(contractId))

}
