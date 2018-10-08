package org.echo.mobile.framework.model.socketoperations

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import org.echo.mobile.framework.Callback

/**
 * Get the chain id.
 *
 * @return Chain id
 *
 * @author Daria Pechkovskaya
 */
class GetChainIdSocketOperation(
    override val apiId: Int,
    callId: Int,
    callback: Callback<String>

) : SocketOperation<String>(SocketMethodType.CALL, callId, String::class.java, callback) {

    override fun createParameters(): JsonElement =
        JsonArray().apply {
            add(apiId)
            add(SocketOperationKeys.CHAIN_ID.key)
            add(JsonArray())
        }

    override fun fromJson(json: String): String? {
        val parser = JsonParser()
        val jsonTree = parser.parse(json)

        try {
            return jsonTree.asJsonObject.get(RESULT_KEY)?.asJsonPrimitive?.asString
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return null
    }
}
