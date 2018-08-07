package com.pixelplex.echoframework.facade

import com.pixelplex.echoframework.Callback
import com.pixelplex.echoframework.model.contract.ContractMethodParameter

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
     * @param contractId Id of contract to call
     * @param methodName Name of method to call
     * @param methodParams Parameters of method to call
     * @param callback Listener of operation results.
     *                 Retrieves true if call succeed, otherwise - false
     */
    fun callContractMethod(
        registrarNameOrId: String,
        password: String,
        assetId: String,
        contractId: String,
        methodName: String,
        methodParams: List<ContractMethodParameter>,
        callback: Callback<Boolean>
    )

}
