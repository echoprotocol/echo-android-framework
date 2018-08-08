package com.pixelplex.echoframework.model.socketoperations

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import com.pixelplex.echoframework.Callback
import com.pixelplex.echoframework.ILLEGAL_ID
import com.pixelplex.echoframework.model.*
import com.pixelplex.echoframework.model.network.Network
import com.pixelplex.echoframework.model.operations.AccountUpdateOperation
import com.pixelplex.echoframework.model.operations.TransferOperation

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
    override val apiId: Int,
    val accountId: String,
    val startId: String = DEFAULT_HISTORY_ID,
    val stopId: String = DEFAULT_HISTORY_ID,
    val limit: Int = DEFAULT_LIMIT,
    val network: Network,
    callId: Int,
    callback: Callback<HistoryResponse>

) : SocketOperation<HistoryResponse>(
    SocketMethodType.CALL,
    callId,
    HistoryResponse::class.java,
    callback
) {

    override fun createParameters(): JsonElement =
        JsonArray().apply {
            add(apiId)
            add(SocketOperationKeys.ACCOUNT_HISTORY.key)
            add(JsonArray().apply {
                add(accountId)
                add(startId)
                add(limit)
                add(stopId)
            })
        }

    override fun fromJson(json: String): HistoryResponse? {
        val gson = configureGson()

        val responseType = object : TypeToken<HistoryResponse>() {
        }.type

        return gson.fromJson<HistoryResponse>(json, responseType)
    }

    private fun configureGson() = GsonBuilder().apply {
        registerTypeAdapter(
            HistoricalTransfer::class.java,
            HistoricalTransfer.HistoryDeserializer()
        )
        registerTypeAdapter(
            AccountUpdateOperation::class.java,
            AccountUpdateOperation.Deserializer()
        )
        registerTypeAdapter(
            TransferOperation::class.java,
            TransferOperation.TransferDeserializer()
        )
        registerTypeAdapter(
            Memo::class.java,
            Memo.MemoDeserializer(network)
        )
        registerTypeAdapter(AssetAmount::class.java, AssetAmount.Deserializer())
        registerTypeAdapter(Authority::class.java, Authority.Deserializer(network))
        registerTypeAdapter(Account::class.java, Account.Deserializer())
        registerTypeAdapter(AccountOptions::class.java, AccountOptions.Deserializer(network))
    }.create()

    companion object {
        const val DEFAULT_HISTORY_ID = "1.11.0"
        const val DEFAULT_LIMIT = 100
    }

}
