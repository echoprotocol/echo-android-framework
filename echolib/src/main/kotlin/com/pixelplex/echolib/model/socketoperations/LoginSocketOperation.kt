package com.pixelplex.echolib.model.socketoperations

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.pixelplex.echolib.Callback
import com.pixelplex.echolib.ILLEGAL_ID

/**
 * Represents blockchain call for access to blockchain
 *
 * @author Daria Pechkovskaya
 */
class LoginSocketOperation(
    val api: Int,
    method: SocketMethodType = SocketMethodType.CALL,
    callback: Callback<Boolean>
) : SocketOperation<Boolean>(method, ILLEGAL_ID, Boolean::class.java, callback) {

    override fun createParameters(): JsonElement =
        JsonArray().apply {
            add(apiId)
            add(SocketOperationKeys.LOGIN.key)
            add(getParameters())
        }

    override val apiId: Int
        get() = api

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
