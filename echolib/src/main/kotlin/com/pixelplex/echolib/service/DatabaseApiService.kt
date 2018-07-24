package com.pixelplex.echolib.service

import com.pixelplex.echolib.AccountListener
import com.pixelplex.echolib.Callback
import com.pixelplex.echolib.model.*
import com.pixelplex.echolib.support.Result

/**
 * Encapsulates logic, associated with blockchain database API
 *
 * <p>
 *     Graphene blockchain database API:
 *     http://docs.bitshares.org/api/database.html
 * </p>
 *
 * @author Dmitriy Bushuev
 */
interface DatabaseApiService : ApiService, AccountsService, GlobalsService,
    AuthorityAndValidationService

/**
 * Encapsulates logic, associated with data from account from blockchain database API
 *
 * <a href="http://docs.bitshares.org/api/database.html#accounts">Source</a>
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
     * Retrieves block information
     *
     * @return chain id string
     */
    fun getBlockData(): BlockData

    /**
     * Retrieves current blockchain block data
     * @return dynamicGlobalProperties
     */
    fun getDynamicGlobalProperties(): Result<Exception, DynamicGlobalProperties>

    /**
     * Registers listener for notifying when account events occur
     *
     * @param id       Account object id
     * @param listener Listener for notifying
     */
    fun subscribeOnAccount(
        id: String,
        listener: AccountListener
    )

    /**
     * Removes all listener, connected with required account [id], from events notifying
     *
     * @param id       Account object id
     * @param callback Listener for notifying
     */
    fun unsubscribeFromAccount(id: String, callback: Callback<Boolean>)

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
