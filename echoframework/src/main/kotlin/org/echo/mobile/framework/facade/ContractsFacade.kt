package org.echo.mobile.framework.facade

import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.model.contract.ContractInfo
import org.echo.mobile.framework.model.contract.ContractResult
import org.echo.mobile.framework.model.contract.ContractStruct
import org.echo.mobile.framework.model.contract.input.InputValue

/**
 * Encapsulates logic, associated with various blockchain smart contract processes
 *
 * @author Daria Pechkovskaya
 */
interface ContractsFacade {

    /**
     * Creates contract on blockchain
     *
     * @param registrarNameOrId Name or id of account that creates the contract
     * @param password          Password from account for transaction signature
     * @param assetId           Asset of contract
     * @param byteCode          Bytecode of the created contract
     * @param gasLimit          Gas limit for contract operation
     * @param gasPrice          One gas price for contract operation
     * @param callback          Listener of operation results.
     *                          Retrieves true if creation succeed, otherwise - false
     */
    fun createContract(
        registrarNameOrId: String,
        password: String,
        assetId: String,
        byteCode: String,
        gasLimit: Long = DEFAULT_GAS_LIMIT,
        gasPrice: Long = DEFAULT_GAS_PRICE,
        callback: Callback<Boolean>
    )

    /**
     * Calls to contract on blockchain
     *
     * @param userNameOrId Name or id of account that calls the contract
     * @param password     Password from account for transaction signature
     * @param assetId      Asset of contract
     * @param contractId   Id of called contract
     * @param methodName   Name of called method
     * @param methodParams Parameters of calling method
     * @param gasLimit     Gas limit for contract operation
     * @param gasPrice     One gas price for contract operation
     * @param callback     Listener of operation results.
     *                     Retrieves true if call succeed, otherwise - false
     */
    fun callContract(
        userNameOrId: String,
        password: String,
        assetId: String,
        contractId: String,
        methodName: String,
        methodParams: List<InputValue>,
        gasLimit: Long = DEFAULT_GAS_LIMIT,
        gasPrice: Long = DEFAULT_GAS_PRICE,
        callback: Callback<Boolean>
    )

    /**
     * Calls contract method without changing state of blockchain
     *
     * @param userNameOrId Name or id of account that calls the contract
     * @param contractId   Id of called contract
     * @param assetId      Asset of contract
     * @param methodName   Name of calling method
     * @param methodParams Parameters of called method
     * @param callback     Listener of operation results.
     */
    fun queryContract(
        userNameOrId: String,
        assetId: String,
        contractId: String,
        methodName: String,
        methodParams: List<InputValue>,
        callback: Callback<String>
    )

    /**
     * Return result of contract operation call
     *
     * @param historyId History operation id
     */
    fun getContractResult(historyId: String, callback: Callback<ContractResult>)

    /**
     * Returns contracts called by ids
     *
     * @param contractIds List of contracts ids
     */
    fun getContracts(contractIds: List<String>, callback: Callback<List<ContractInfo>>)

    /**
     * Returns all existing contracts from blockchain
     */
    fun getAllContracts(callback: Callback<List<ContractInfo>>)

    /**
     * Return full information about contract
     *
     * @param contractId Id of contract
     */
    fun getContract(contractId: String, callback: Callback<ContractStruct>)

    companion object {
        private const val DEFAULT_GAS_LIMIT = 1000000L
        private const val DEFAULT_GAS_PRICE = 0L
    }

}
