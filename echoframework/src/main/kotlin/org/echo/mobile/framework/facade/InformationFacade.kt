package org.echo.mobile.framework.facade

import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.model.AccountBalance
import org.echo.mobile.framework.model.FullAccount
import org.echo.mobile.framework.model.GlobalProperties
import org.echo.mobile.framework.model.HistoryResponse

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
     * Fetches accounts associated with private key in wif format [wif]
     *
     * Calls [callback]'s success method with list, contains all received accounts
     */
    fun getAccountsByWif(wif: String, callback: Callback<List<FullAccount>>)

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
    fun getBalance(nameOrId: String, asset: String, callback: Callback<AccountBalance>)

    /**
     * Retrieves account history with defining setting parameters
     *
     * @param nameOrId             Source account name or id
     * @param transactionStartId   ID of the most recent operation to retrieve
     * @param transactionStopId    ID of the earliest operation to retrieve
     * @param limit                Maximum number of operations to retrieve
     * @param callback             Listener of operation results
     */
    fun getAccountHistory(
        nameOrId: String,
        transactionStartId: String,
        transactionStopId: String,
        limit: Int,
        callback: Callback<HistoryResponse>
    )

    /**
     * Retrieves blockchain current configuration parameters
     */
    fun getGlobalProperties(callback: Callback<GlobalProperties>)

}
