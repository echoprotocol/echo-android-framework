package com.pixelplex.echolib.service

import com.pixelplex.echolib.Callback
import com.pixelplex.echolib.ILLEGAL_ID
import com.pixelplex.echolib.model.Account

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
interface DatabaseApiService : ApiService, AccountsService {

    companion object {
        /**
         * Actual id for DatabaseApi
         */
        @Volatile
        var id: Int = ILLEGAL_ID
    }
}

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
        callback: Callback<Map<String, Account>>
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
    ): Map<String, Account>
}
