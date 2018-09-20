package com.pixelplex.echoframework.facade

import com.pixelplex.echoframework.Callback
import com.pixelplex.echoframework.model.Account
import com.pixelplex.echoframework.model.Balance
import com.pixelplex.echoframework.model.FullAccount
import com.pixelplex.echoframework.model.HistoryResponse

/**
 * Encapsulates logic, associated with receiving blockchain information processes
 *
 * @author Dmitriy Bushuev
 */
interface InformationFacade {

    /**
     * Find and returns account with required [nameOrId] if exists
     *
     * @param nameOrId Required account name or id
     * @param callback Listener of operation results.
     *                 Receives success result only when required account exists
     */
    fun getAccount(nameOrId: String, callback: Callback<FullAccount>)

    /**
     * Checks whether account with [nameOrId] already exists
     *
     * @param nameOrId Required account name or id
     * @param callback Listener of operation results.
     *                 Retrieves true, if account is available, otherwise false
     */
    fun checkAccountReserved(nameOrId: String, callback: Callback<Boolean>)

    /**
     * Gets balance for account with defined [nameOrId] and specific asset type
     *
     * @param nameOrId Required account name or id
     * @param asset    Specific asset type id
     * @param callback Listener of operation results
     */
    fun getBalance(nameOrId: String, asset: String, callback: Callback<Balance>)

    /**
     * Retrieves account history with defining setting parameters
     *
     * @param nameOrId             Source account name or id
     * @param transactionStartId   ID of the most recent operation to retrieve
     * @param transactionStopId    ID of the earliest operation to retrieve
     * @param limit                Maximum number of operations to retrieve
     * @param asset                Specific asset type id
     * @param callback             Listener of operation results
     */
    fun getAccountHistory(
        nameOrId: String,
        transactionStartId: String,
        transactionStopId: String,
        limit: Int,
        asset: String,
        callback: Callback<HistoryResponse>
    )

}
