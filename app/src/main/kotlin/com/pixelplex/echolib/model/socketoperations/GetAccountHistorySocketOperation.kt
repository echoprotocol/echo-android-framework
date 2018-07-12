package com.pixelplex.echolib.model.socketoperations

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.pixelplex.echolib.model.HistoricalTransfer

/**
 * Get operations relevant to the specified account.
 *
 * @param accountId The account whose history should be queried
 * @param stopId Id of the earliest operation to retrieve
 * @param limit Maximum number of operations to retrieve (must not exceed 100)
 * @param startId Id of the most recent operation to retrieve
 *
 * @return A list of [HistoricalTransfer] objects performed by account, ordered from most recent
 * to oldest.
 *
 * @author Daria Pechkovskaya
 */
class GetAccountHistorySocketOperation(
    method: SocketMethodType = SocketMethodType.CALL,
    callId: Int,
    apiId: Int,
    result: OperationResult<List<HistoricalTransfer>>,
    val accountId: String,
    val stopId: String = DEFAULT_HISTORY_ID,
    val limit: Int = DEFAULT_LIMIT,
    val startId: String = DEFAULT_HISTORY_ID

) : SocketOperation(method, callId, apiId, result) {

    override fun createParameters(): JsonElement =
        JsonArray().apply {
            add(apiId)
            add(SocketOperationKeys.ACCOUNT_HISTORY.key)
            add(JsonArray().apply {
                add(accountId)
                add(stopId)
                add(limit)
                add(startId)
            })
        }


    companion object {
        const val DEFAULT_HISTORY_ID = "1.11.0"
        const val DEFAULT_LIMIT = 100
    }
}
