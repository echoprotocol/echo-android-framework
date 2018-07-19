package com.pixelplex.echolib.model.socketoperations

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.pixelplex.echolib.Callback
import com.pixelplex.echolib.ILLEGAL_ID
import com.pixelplex.echolib.support.Api
import com.pixelplex.echolib.support.getId

/**
 * Get the chain id.
 *
 * @return Chain id
 *
 * @author Daria Pechkovskaya
 */
class GetChainIdSocketOperation(
    val api: Api,
    method: SocketMethodType = SocketMethodType.CALL,
    callback: Callback<String>

) : SocketOperation<String>(method, ILLEGAL_ID, String::class.java, callback) {

    override fun createParameters(): JsonElement =
        JsonArray().apply {
            add(apiId)
            add(SocketOperationKeys.CHAIN_ID.key)
            add(JsonArray())
        }

    override val apiId: Int
        get() = api.getId()

    override fun fromJson(json: String): String? {
        val parser = JsonParser()
        val jsonTree = parser.parse(json)

        try {
            val result = jsonTree.asJsonObject.get("result")?.asJsonPrimitive?.asString

            return result

        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return null
    }
}
