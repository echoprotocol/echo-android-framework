package com.pixelplex.echoframework.model.socketoperations

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.pixelplex.echoframework.Callback
import com.pixelplex.echoframework.model.contract.ContractResult

/**
 * Retrieves result of called contract operation
 *
 * @param resultId Operation result id
 *
 * @author Daria Pechkovskaya
 */
class GetContractResultSocketOperation(
    override val apiId: Int,
    private val resultId: String,
    callId: Int,
    callback: Callback<ContractResult>

) : SocketOperation<ContractResult>(
    SocketMethodType.CALL,
    callId,
    ContractResult::class.java,
    callback
) {

    override fun createParameters(): JsonElement =
        JsonArray().apply {
            add(apiId)
            add(SocketOperationKeys.GET_CONTRACT_RESULT.key)
            add(JsonArray().apply { add(resultId) })
        }

    override fun fromJson(json: String): ContractResult? {
        val parser = JsonParser()
        val jsonTree = parser.parse(json)

        if (!jsonTree.isJsonObject || jsonTree.asJsonObject.get(RESULT_KEY) == null) {
            return null
        }

        val result = jsonTree.asJsonObject.get(RESULT_KEY)?.asJsonObject

        return GsonBuilder().create().fromJson<ContractResult>(result, type)
    }
}
