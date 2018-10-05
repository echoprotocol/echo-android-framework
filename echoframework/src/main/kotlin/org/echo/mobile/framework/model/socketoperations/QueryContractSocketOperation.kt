package org.echo.mobile.framework.model.socketoperations

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import org.echo.mobile.framework.Callback

/**
 * Calls contract operation without chaining blockchain state. Retrieves constant information.
 *
 * @param contractId Id of called contract
 * @param registrarId Id of account which call contract
 * @param assetId Id of contract asset
 * @param code Called code of operation on contract
 *
 * @author Daria Pechkovskaya
 */
class QueryContractSocketOperation(
    override val apiId: Int,
    private val contractId: String,
    private val registrarId: String,
    private val assetId: String,
    private val code: String,
    callId: Int,
    method: SocketMethodType = SocketMethodType.CALL,
    callback: Callback<String>
) : SocketOperation<String>(
    method,
    callId,
    String::class.java,
    callback
) {

    override fun createParameters(): JsonElement =
        JsonArray().apply {
            add(apiId)
            add(SocketOperationKeys.CALL_CONTRACT_NO_CHANGING_STATE.key)
            add(JsonArray().apply {
                add(contractId)
                add(registrarId)
                add(assetId)
                add(code)
            })
        }

    override fun fromJson(json: String): String? {
        val parser = JsonParser()
        val jsonTree = parser.parse(json)

        if (!jsonTree.isJsonObject || jsonTree.asJsonObject.get(RESULT_KEY) == null) {
            return null
        }

        return jsonTree.asJsonObject.get(RESULT_KEY)?.asJsonPrimitive?.asString
    }
}
