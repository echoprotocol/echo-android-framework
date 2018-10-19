package org.echo.mobile.framework.facade.internal

import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.core.crypto.CryptoCoreComponent
import org.echo.mobile.framework.exception.LocalException
import org.echo.mobile.framework.facade.ContractsFacade
import org.echo.mobile.framework.model.Account
import org.echo.mobile.framework.model.AuthorityType
import org.echo.mobile.framework.model.Transaction
import org.echo.mobile.framework.model.contract.ContractInfo
import org.echo.mobile.framework.model.contract.ContractResult
import org.echo.mobile.framework.model.contract.ContractStruct
import org.echo.mobile.framework.model.contract.input.ContractInputEncoder
import org.echo.mobile.framework.model.contract.input.InputValue
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
        gasLimit: Long,
        gasPrice: Long,
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

        checkOwnerAccount(registrar!!.name, password, registrar!!)

        val privateKey = cryptoCoreComponent.getPrivateKey(
            registrar!!.name,
            password,
            AuthorityType.ACTIVE
        )

        val contractOperation = ContractOperationBuilder()
            .setAsset(assetId)
            .setRegistrar(registrar!!)
            .setGas(gasLimit)
            .setGasPrice(gasPrice)
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
        userNameOrId: String,
        password: String,
        assetId: String,
        contractId: String,
        methodName: String,
        methodParams: List<InputValue>,
        gasLimit: Long,
        gasPrice: Long,
        callback: Callback<Boolean>
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

    override fun getContracts(contractIds: List<String>, callback: Callback<List<ContractInfo>>) =
        callback.processResult(databaseApiService.getContracts(contractIds))

    override fun getAllContracts(callback: Callback<List<ContractInfo>>) =
        callback.processResult(databaseApiService.getAllContracts())

    override fun getContract(contractId: String, callback: Callback<ContractStruct>) =
        callback.processResult(databaseApiService.getContract(contractId))

}
