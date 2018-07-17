package com.pixelplex.echolib.model.socketoperations

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.pixelplex.echolib.Callback
import com.pixelplex.echolib.ILLEGAL_ID
import com.pixelplex.echolib.model.HistoricalTransfer
import com.pixelplex.echolib.support.Api
import com.pixelplex.echolib.support.getId

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
    val api: Api,
    val accountId: String,
    val stopId: String = DEFAULT_HISTORY_ID,
    val limit: Int = DEFAULT_LIMIT,
    val startId: String = DEFAULT_HISTORY_ID,
    method: SocketMethodType = SocketMethodType.CALL,
    callback: Callback<List<HistoricalTransfer>>

) : SocketOperation<List<HistoricalTransfer>>(method, ILLEGAL_ID, listOf<HistoricalTransfer>().javaClass, callback) {

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

    override val apiId: Int
        get() = api.getId()

    override fun fromJson(json: String): List<HistoricalTransfer> {
        return emptyList()
    }

    companion object {
        const val DEFAULT_HISTORY_ID = "1.11.0"
        const val DEFAULT_LIMIT = 100
    }

}
