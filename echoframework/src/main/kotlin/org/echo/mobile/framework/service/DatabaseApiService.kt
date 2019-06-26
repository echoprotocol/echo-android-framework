package org.echo.mobile.framework.service

import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.core.mapper.ObjectMapper
import org.echo.mobile.framework.exception.LocalException
import org.echo.mobile.framework.model.Asset
import org.echo.mobile.framework.model.AssetAmount
import org.echo.mobile.framework.model.BaseOperation
import org.echo.mobile.framework.model.Block
import org.echo.mobile.framework.model.BlockData
import org.echo.mobile.framework.model.DynamicGlobalProperties
import org.echo.mobile.framework.model.EthAddress
import org.echo.mobile.framework.model.EthDeposit
import org.echo.mobile.framework.model.EthWithdraw
import org.echo.mobile.framework.model.FullAccount
import org.echo.mobile.framework.model.GlobalProperties
import org.echo.mobile.framework.model.GrapheneObject
import org.echo.mobile.framework.model.Log
import org.echo.mobile.framework.model.contract.ContractFee
import org.echo.mobile.framework.model.contract.ContractInfo
import org.echo.mobile.framework.model.contract.ContractResult
import org.echo.mobile.framework.model.contract.ContractStruct
import org.echo.mobile.framework.support.Result

/**
 * Encapsulates logic, associated with blockchain database API
 *
 * [Graphene blockchain database API](https://dev-doc.myecho.app/classgraphene_1_1app_1_1database__api.html)
 *
 * @author Dmitriy Bushuev
 */
interface DatabaseApiService : ApiService, AccountsService, GlobalsService,
    AuthorityAndValidationService, BlocksAndTransactionsService, ContractsService, AssetsService,
    SubscriptionService, ObjectsService, CustomOperationService

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

    /**
     * Fetches accounts associated with private keys in wifs format [wifs]
     *
     * Calls [callback]'s success method with map, contains pairs wif -> accounts list, associated with this wif
     */
    fun getAccountsByWif(wifs: List<String>, callback: Callback<Map<String, List<FullAccount>>>)

    /**
     * Fetches accounts associated with private keys in wifs format [wifs]
     *
     * Returns map, contains pairs wif -> accounts list, associated with this wif
     */
    fun getAccountsByWif(wifs: List<String>): Result<LocalException, Map<String, List<FullAccount>>>

    /**
     * Fetches addresses list [EthAddress] for required account [accountId]
     */
    fun getEthereumAddress(accountId: String, callback: Callback<EthAddress>)

    /**
     * Retrieves list of account's [accountId] deposits [EthDeposit]
     */
    fun getAccountDeposits(accountId: String, callback: Callback<List<EthDeposit>>)

    /**
     * Retrieves list of account's [accountId] withdrawals [EthWithdraw]
     */
    fun getAccountWithdrawals(accountId: String, callback: Callback<List<EthWithdraw>>)
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
     * Retrieves blockchain chain id
     */
    fun getChainId(callback: Callback<String>)

    /**
     * Retrieves current blockchain block data
     * @return dynamicGlobalProperties
     */
    fun getDynamicGlobalProperties(): Result<Exception, DynamicGlobalProperties>

    /**
     * Retrieves current blockchain block data
     */
    fun getDynamicGlobalProperties(callback: Callback<DynamicGlobalProperties>)

    /**
     * Retrieves blockchain current configuration parameters
     */
    fun getGlobalProperties(callback: Callback<GlobalProperties>)

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

    /**
     * Retrieves required fee by asset for ech operation
     *
     * @param operations Operations for getting fee
     * @param asset Asset type for fee paying
     *
     * @return [AssetAmount] fees for each operation
     */
    fun getRequiredContractFees(
        operations: List<BaseOperation>,
        asset: Asset
    ): Result<Exception, List<ContractFee>>
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
     * Retrieves base block information
     */
    fun getBlockData(callback: Callback<BlockData>)

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
 * Encapsulates logic, associated with assets information from Database API
 */
interface AssetsService {

    /**
     * Query list of assets by required asset symbol [lowerBound] with limit [limit]
     */
    fun listAssets(lowerBound: String, limit: Int, callback: Callback<List<Asset>>)

    /**
     * Query list of assets by it's ids [assetIds]
     */
    fun getAssets(assetIds: List<String>, callback: Callback<List<Asset>>)

    /**
     * Query list of assets by it's ids [assetIds]
     */
    fun getAssets(assetIds: List<String>): Result<LocalException, List<Asset>>

    /**
     * Query list of assets by it's [symbolsOrIds]
     */
    fun lookupAssetsSymbols(symbolsOrIds: List<String>, callback: Callback<List<Asset>>)

    /**
     * Query list of assets by it's [symbolsOrIds] and wraps with [Result]
     */
    fun lookupAssetsSymbols(symbolsOrIds: List<String>): Result<LocalException, List<Asset>>

}

/**
 * Encapsulates logic, associated with information about contracts from Database API
 */
interface ContractsService {

    /**
     * Calls contract without blockchain changing state
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
     * Return list of contract logs
     *
     * @param contractId   Contract id for fetching logs
     * @param fromBlock    Number of the earliest block to retrieve
     * @param toBlock      Number of the most recent block to retrieve
     */
    fun getContractLogs(
        contractId: String,
        fromBlock: String,
        toBlock: String
    ): Result<LocalException, List<Log>>

    /**
     * Returns contracts by ids
     *
     * @param contractIds List of contracts ids
     */
    fun getContracts(contractIds: List<String>): Result<LocalException, List<ContractInfo>>

    /**
     * Returns contract code by [contractId]
     *
     * @param contractId Id of contract
     */
    fun getContract(contractId: String): Result<LocalException, ContractStruct>

}

/**
 * Encapsulates logic, associated with global subscription to blockchain objects
 */
interface SubscriptionService {

    /**
     * Subscribes to listening chain objects
     * Clears all subscriptions when [clearFilter] set to true
     */
    fun subscribe(clearFilter: Boolean, callback: Callback<Boolean>)

    /**
     * Subscribes from listening chain objects
     */
    fun unsubscribe(callback: Callback<Boolean>)

    /**
     * Subscribes to listening contract logs
     *
     * @param contractId   Contract id for fetching logs
     * @param fromBlock    Number of the earliest block to retrieve
     * @param toBlock      Number of the most recent block to retrieve
     */
    fun subscribeContractLogs(
        contractId: String,
        fromBlock: String,
        toBlock: String
    ): Result<LocalException, List<Log>>

    /**
     * Subscribes to listening contracts changes
     *
     * @param contractIds Ids of contracts for listening
     */
    fun subscribeContracts(contractIds: List<String>): Result<LocalException, Boolean>
}

/**
 * Encapsulates logic, associated with retrieving blockchain objects
 */
interface ObjectsService {

    /**
     * Return objects, required by [ids]
     *
     * @param mapper Maps object data by [ObjectMapper]
     */
    fun <T : GrapheneObject> getObjects(
        ids: List<String>,
        mapper: ObjectMapper<T>
    ): Result<Exception, List<T>>

}
