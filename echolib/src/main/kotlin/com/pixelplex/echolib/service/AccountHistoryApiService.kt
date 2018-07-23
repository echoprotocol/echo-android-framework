package com.pixelplex.echolib.service

import com.pixelplex.echolib.Callback
import com.pixelplex.echolib.model.HistoryResponse

/**
 * Encapsulates logic, associated with blockchain account history API
 *
 * <p>
 *     Graphene blockchain account history API:
 *     http://docs.bitshares.org/api/history.html
 * </p>
 *
 * @author Dmitriy Bushuev
 */
interface AccountHistoryApiService : ApiService {

    /**
     * Get operations relevant to the specified account
     *
     * @param accountId: The account whose history should be queried
     * @param start: ID of the most recent operation to retrieve
     * @param stop: ID of the earliest operation to retrieve
     * @param limit: Maximum number of operations to retrieve (must not exceed 100)
     * @return A list of operations performed by account, ordered from most recent to oldest
     */
    fun getAccountHistory(
        accountId: String,
        start: String,
        stop: String,
        limit: Int,
        callback: Callback<HistoryResponse>
    )

}
