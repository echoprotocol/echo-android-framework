package com.pixelplex.echoframework.service

import com.pixelplex.echoframework.AccountListener
import com.pixelplex.echoframework.Callback
import com.pixelplex.echoframework.exception.LocalException
import com.pixelplex.echoframework.model.*
import com.pixelplex.echoframework.model.contract.ContractInfo
import com.pixelplex.echoframework.model.contract.ContractResult
import com.pixelplex.echoframework.model.contract.ContractStruct
import com.pixelplex.echoframework.support.Result

/**
 * Encapsulates logic, associated with blockchain database API
 *
 * [Graphene blockchain database API](https://dev-doc.myecho.app/classgraphene_1_1app_1_1database__api.html)
 *
 * @author Dmitriy Bushuev
 */
interface DatabaseApiService : ApiService, AccountsService, GlobalsService,
    AuthorityAndValidationService, BlocksAndTransactionsService, ContractsService

/**
 * Encapsulates logic, associated with data from account from blockchain database API
 *
 * {@see http://docs.bitshares.org/api/database.html#accounts}
 */
interface AccountsService {

    /**
     * Fetch all objects relevant to the specified accounts and subscribe to updates.
     *
     * @param namesOrIds Each item must be the name or ID of an account to retrieve
     * @param subscribe Flag for subscribe options, true if need to subscribe on changes
     * @param callback Async listening result
     */
    fun getFullAccounts(
        namesOrIds: List<String>,
        subscribe: Boolean,
        callback: Callback<Map<String, FullAccount>>
    )

    /**
     * Fetch all objects relevant to the specified accounts and subscribe to updates.
     *
     * @param namesOrIds Each item must be the name or ID of an account to retrieve
     * @param subscribe Flag for subscribe options, true if need to subscribe on changes
     * @return Synchronized return result
     */
    fun getFullAccounts(
        namesOrIds: List<String>,
        subscribe: Boolean
    ): Result<Exception, Map<String, FullAccount>>
}

/**
 * Encapsulates logic, associated with global data from Database API
 */
interface GlobalsService {

    /**
     * Retrieves blockchain chain id
     * @return chain id string
     */
    fun getChainId(): Result<Exception, String>

    /**
     * Retrieves current blockchain block data
     * @return dynamicGlobalProperties
     */
    fun getDynamicGlobalProperties(): Result<Exception, DynamicGlobalProperties>

    /**
     * Registers listener for notifying when account events occur
     *
     * @param nameOrId       Account object id or name
     * @param listener Listener for notifying
     */
    fun subscribeOnAccount(
        nameOrId: String,
        listener: AccountListener
    )

    /**
     * Removes all listener, connected with required account [nameOrId], from events notifying
     *
     * @param v       Account object id or name
     * @param callback Listener for notifying
     */
    fun unsubscribeFromAccount(nameOrId: String, callback: Callback<Boolean>)

    /**
     * Removes all registered listeners
     *
     * @param callback Listener for notifying
     */
    fun unsubscribeAll(callback: Callback<Boolean>)

}

/**
 * Encapsulates logic, associated with authority and validation data from Database API
 */
interface AuthorityAndValidationService {

    /**
     * Retrieves required fee by asset for ech operation
     *
     * @param operations Operations for getting fee
     * @param asset Asset type for fee paying
     *
     * @return [AssetAmount] fees for each operation
     */
    fun getRequiredFees(
        operations: List<BaseOperation>,
        asset: Asset
    ): Result<Exception, List<AssetAmount>>
}

/**
 * Encapsulates logic, associated with blocks and transactions information from Database API
 */
interface BlocksAndTransactionsService {

    /**
     * Retrieves base block information
     *
     * @return current block data
     */
    fun getBlockData(): BlockData

    /**
     * Retrieves full signed block
     *
     * @param blockNumber Height of the block to be returned
     * @param callback Listener for notifying
     */
    fun getBlock(blockNumber: String, callback: Callback<Block>)

    /**
     * Retrieves full signed block synchronously
     *
     * @param blockNumber Height of the block to be returned
     */
    fun getBlock(blockNumber: String): Result<LocalException, Block>

}

/**
 * Encapsulates logic, associated with information about contracts from Database API
 */
interface ContractsService {

    /**
     * Calls contract without changing state of blockchain
     *
     * @param contractId Id of contract to call
     * @param registrarNameOrId Name or id of account caller
     * @param assetId Asset id of contract
     * @param byteCode Code calling to contract
     */
    fun callContractNoChangingState(
        contractId: String,
        registrarNameOrId: String,
        assetId: String,
        byteCode: String
    ): Result<LocalException, String>

    /**
     * Return result of contract operation call
     *
     * @param historyId History operation id
     */
    fun getContractResult(historyId: String): Result<LocalException, ContractResult>

    /**
     * Returns all contracts from blockchain
     */
    fun getAllContracts(): Result<LocalException, List<ContractInfo>>

    /**
     * Returns contracts by ids
     *
     * @param contractIds List of contracts ids
     */
    fun getContracts(contractIds: List<String>): Result<LocalException, List<ContractInfo>>

    /**
     * Return full information about contract
     *
     * @param contractId Id of contract
     */
    fun getContract(contractId: String): Result<LocalException, ContractStruct>
}
