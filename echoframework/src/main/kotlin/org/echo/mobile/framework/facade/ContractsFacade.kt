package org.echo.mobile.framework.facade

import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.model.contract.ContractInfo
import org.echo.mobile.framework.model.contract.ContractLog
import org.echo.mobile.framework.model.contract.ContractResult
import org.echo.mobile.framework.model.contract.ContractStruct
import org.echo.mobile.framework.model.contract.input.InputValue
import java.math.BigInteger

/**
 * Encapsulates logic, associated with various blockchain smart contract processes
 *
 * @author Daria Pechkovskaya
 */
interface ContractsFacade {

    /**
     * Creates contract on blockchain
     *
     * @param registrarNameOrId     Name or id of account that creates the contract
     * @param wif                   Account's private key in wif format
     * @param value                 Payable value for contract creation
     * @param assetId               Asset of contract
     * @param feeAsset              Asset for fee pay
     * @param byteCode              Bytecode of the created contract
     * @param params                Params for contract constructor
     * @param broadcastCallback     Callback for result of operation broadcast
     * @param resultCallback        Callback for retrieving result of operation (not required).
     *                              Retrieves result of transactions if exists -
     *                              history id which contains call contract result,
     *                              if not exists - empty string
     */
    fun createContract(
        registrarNameOrId: String,
        wif: String,
        value: String = BigInteger.ZERO.toString(),
        assetId: String,
        feeAsset: String?,
        byteCode: String,
        params: List<InputValue> = listOf(),
        broadcastCallback: Callback<Boolean>,
        resultCallback: Callback<String>? = null
    )

    /**
     * Calls to contract on blockchain using account private key wif
     *
     * @param userNameOrId          Name or id of account that calls the contract
     * @param wif                   Account's private key in wif format
     * @param assetId               Asset of contract
     * @param feeAsset              Asset for fee pay
     * @param contractId            Id of called contract
     * @param methodName            Name of called method
     * @param methodParams          Parameters of calling method
     * @param value                 Amount for payable methods
     * @param broadcastCallback     Callback for result of operation deploying
     * @param resultCallback        Callback for retrieving result of operation (not required).
     *                              Retrieves result of transactions if exists -
     *                              history id which contains call contract result,
     *                              if not exists - empty string
     */
    fun callContract(
        userNameOrId: String,
        wif: String,
        assetId: String,
        feeAsset: String?,
        contractId: String,
        methodName: String,
        methodParams: List<InputValue>,
        value: String = BigInteger.ZERO.toString(),
        broadcastCallback: Callback<Boolean>,
        resultCallback: Callback<String>? = null
    )

    /**
     * Calls to contract on blockchain using account private key wif
     *
     * Required valid [code] for contract call
     *
     * @param userNameOrId          Name or id of account that calls the contract
     * @param wif                   Account's private key in wif format
     * @param assetId               Asset of contract
     * @param feeAsset              Asset for fee pay
     * @param contractId            Id of called contract
     * @param code                  Valid code for contract call
     * @param value                 Amount for payable methods
     * @param broadcastCallback     Callback for result of operation deploying
     * @param resultCallback        Callback for retrieving result of operation (not required).
     *                              Retrieves result of transactions if exists -
     *                              history id which contains call contract result,
     *                              if not exists - empty string
     */
    fun callContract(
        userNameOrId: String,
        wif: String,
        assetId: String,
        feeAsset: String?,
        contractId: String,
        code: String,
        value: String = BigInteger.ZERO.toString(),
        broadcastCallback: Callback<Boolean>,
        resultCallback: Callback<String>? = null
    )

    /**
     * Calls contract method without changing state of blockchain
     *
     * @param userNameOrId Name or id of account that calls the contract
     * @param contractId   Id of called contract
     * @param assetId      Asset of contract
     * @param amount       Value in [assetId]
     * @param methodName   Name of calling method
     * @param methodParams Parameters of called method
     * @param callback     Listener of operation results.
     */
    fun queryContract(
        userNameOrId: String,
        assetId: String,
        amount: String,
        contractId: String,
        methodName: String,
        methodParams: List<InputValue>,
        callback: Callback<String>
    )

    /**
     * Calls contract method without changing state of blockchain
     *
     * @param userNameOrId Name or id of account that calls the contract
     * @param contractId   Id of called contract
     * @param assetId      Asset of contract
     * @param amount       Value in [assetId]
     * @param code         Valid code for contract query
     * @param callback     Listener of operation results.
     */
    fun queryContract(
        userNameOrId: String,
        assetId: String,
        amount: String,
        contractId: String,
        code: String,
        callback: Callback<String>
    )

    /**
     * Return result of contract operation call
     *
     * @param historyId History operation id
     */
    fun getContractResult(historyId: String, callback: Callback<ContractResult>)

    /**
     * Return list of contract logs
     *
     * @param contractId   Contract id for fetching logs
     * @param fromBlock    Number of the earliest block to retrieve
     * @param toBlock      Last request block
     * @param callback     Listener of operation results.
     */
    fun getContractLogs(
        contractId: String,
        fromBlock: String,
        toBlock: String,
        callback: Callback<List<ContractLog>>
    )

    /**
     * Returns contracts called by ids
     *
     * @param contractIds List of contracts ids
     */
    fun getContracts(contractIds: List<String>, callback: Callback<List<ContractInfo>>)

    /**
     * Return contract code by [contractId]
     *
     * @param contractId Id of contract
     */
    fun getContract(contractId: String, callback: Callback<ContractStruct>)

}
