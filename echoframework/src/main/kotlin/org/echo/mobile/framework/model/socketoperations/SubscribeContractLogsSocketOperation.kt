package org.echo.mobile.framework.model.socketoperations

import com.google.common.reflect.TypeToken
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.model.Log

/**
 * Register subscription callback to contract logs.
 * Every notification initiated by the full node will carry a particular id as defined by the
 * user with the identifier parameter.
 *
 * @param contractId Contract id for subscribe to logs
 * @param fromBlock Number of the earliest block to subscribe
 * @param toBlock Number of the most recent block to subscribe
 * @return List of last logs
 *
 * @author Daria Pechkovskaya
 */
class SubscribeContractLogsSocketOperation(
    override val apiId: Int,
    private val contractId: String,
    private val fromBlock: String,
    private val limit: String,
    callId: Int,
    callback: Callback<List<Log>>
) : SocketOperation<List<Log>>(SocketMethodType.CALL, callId, listOf<Log>().javaClass, callback) {

    override fun createParameters(): JsonElement =
        JsonArray().apply {
            add(apiId)
            add(SocketOperationKeys.SUBSCRIBE_CONTRACT_LOGS.key)
            add(JsonArray().apply {
                add(callId)
                add(contractId)
                add(fromBlock)
                add(limit)
            })
        }

    override fun fromJson(json: String): List<Log> {
        val parser = JsonParser()
        val jsonTree = parser.parse(json)

        if (!jsonTree.isJsonObject || jsonTree.asJsonObject.get(RESULT_KEY) == null) {
            return emptyList()
        }

        val result = jsonTree.asJsonObject.get(RESULT_KEY)?.asJsonArray
        return GsonBuilder().create().fromJson<List<Log>>(
            result,
            object : TypeToken<List<Log>>() {}.type
        )
    }

}
