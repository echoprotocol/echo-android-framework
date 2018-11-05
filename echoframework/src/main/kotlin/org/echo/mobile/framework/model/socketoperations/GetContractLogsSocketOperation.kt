package org.echo.mobile.framework.model.socketoperations

import com.google.common.reflect.TypeToken
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.model.Log

/**
 * Retrieves emitted logs of contract
 *
 * @param contractId Contract id for fetching logs
 * @param fromBlock Number of the earliest block to retrieve
 * @param toBlock Number of the most recent block to retrieve
 *
 * @author Daria Pechkovskaya
 */
class GetContractLogsSocketOperation(
    override val apiId: Int,
    private val contractId: String,
    private val fromBlock: String,
    private val toBlock: String,
    callId: Int,
    callback: Callback<List<Log>>

) : SocketOperation<List<Log>>(
    SocketMethodType.CALL,
    callId,
    listOf<Log>().javaClass,
    callback
) {

    override fun createParameters(): JsonElement =
        JsonArray().apply {
            add(apiId)
            add(SocketOperationKeys.GET_CONTRACT_LOGS.key)
            add(JsonArray().apply {
                add(contractId)
                add(fromBlock)
                add(toBlock)
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
