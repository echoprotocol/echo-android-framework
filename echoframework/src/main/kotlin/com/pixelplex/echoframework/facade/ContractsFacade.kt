package com.pixelplex.echoframework.facade

import com.pixelplex.echoframework.Callback
import com.pixelplex.echoframework.model.contract.ContractInfo
import com.pixelplex.echoframework.model.contract.ContractMethodParameter
import com.pixelplex.echoframework.model.contract.ContractResult
import com.pixelplex.echoframework.model.contract.ContractStruct

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
     * @param password Password from account for transaction signature
     * @param assetId Asset of contract
     * @param byteCode Bytecode of the created contract
     * @param callback Listener of operation results.
     *                 Retrieves true if creation succeed, otherwise - false
     */
    fun createContract(
        registrarNameOrId: String,
        password: String,
        assetId: String,
        byteCode: String,
        callback: Callback<Boolean>
    )

    /**
     * Calls to contract on blockchain
     *
     * @param registrarNameOrId Name or id of account that creates the contract
     * @param password Password from account for transaction signature
     * @param assetId Asset of contract
     * @param contractId Id of called contract
     * @param methodName Name of called method
     * @param methodParams Parameters of called method
     * @param callback Listener of operation results.
     *                 Retrieves true if call succeed, otherwise - false
     */
    fun callContract(
        registrarNameOrId: String,
        password: String,
        assetId: String,
        contractId: String,
        methodName: String,
        methodParams: List<ContractMethodParameter>,
        callback: Callback<Boolean>
    )

    /**
     * Calls contract method without changing state of blockchain
     *
     * @param registrarNameOrId Name or id of account that creates the contract
     * @param contractId Id of called contract
     * @param assetId Asset of contract
     * @param methodName Name of called method
     * @param methodParams Parameters of called method
     * @param callback Listener of operation results.
     */
    fun queryContract(
        registrarNameOrId: String,
        assetId: String,
        contractId: String,
        methodName: String,
        methodParams: List<ContractMethodParameter>,
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
}
