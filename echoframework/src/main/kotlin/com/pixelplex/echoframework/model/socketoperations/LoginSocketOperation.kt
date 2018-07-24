package com.pixelplex.echoframework.model.socketoperations

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.pixelplex.echoframework.Callback
import com.pixelplex.echoframework.ILLEGAL_ID

/**
 * Represents blockchain call for access to blockchain
 *
 * @author Daria Pechkovskaya
 */
class LoginSocketOperation(
    override val apiId: Int,
    method: SocketMethodType = SocketMethodType.CALL,
    callback: Callback<Boolean>
) : SocketOperation<Boolean>(method, ILLEGAL_ID, Boolean::class.java, callback) {

    override fun createParameters(): JsonElement =
        JsonArray().apply {
            add(apiId)
            add(SocketOperationKeys.LOGIN.key)
            add(getParameters())
        }

    private fun getParameters(): JsonElement =
        JsonArray().apply {
            add("")
            add("")
        }

    override fun fromJson(json: String): Boolean? {
        val parser = JsonParser()
        val jsonTree = parser.parse(json)

        if (!jsonTree.isJsonObject || jsonTree.asJsonObject.get("result") == null) {
            return null
        }

        return jsonTree.asJsonObject.get("result")?.asJsonPrimitive?.asBoolean
    }
}
