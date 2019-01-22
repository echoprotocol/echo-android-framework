package org.echo.mobile.framework.model.socketoperations

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.ILLEGAL_ID

/**
 * Represents blockchain call for access to blockchain apis
 *
 * @author Daria Pechkovskaya
 */
class AccessSocketOperation(
    val accessSocketType: AccessSocketOperationType,
    val api: Int,
    callId: Int,
    callback: Callback<Int>
) : SocketOperation<Int>(SocketMethodType.CALL, callId, Int::class.java, callback) {

    override fun createParameters(): JsonElement =
        JsonArray().apply {
            add(apiId)
            add(accessSocketType.key)
            add(JsonArray())
        }

    override val apiId: Int
        get() = api

    override fun fromJson(json: String): Int? {
        val parser = JsonParser()
        val jsonTree = parser.parse(json)

        if (!jsonTree.isJsonObject || jsonTree.asJsonObject.get(RESULT_KEY) == null) {
            return null
        }

        return jsonTree.asJsonObject.get(RESULT_KEY)?.asJsonPrimitive?.asInt
    }
}

/**
 * Type of operation for access to blockchain
 */
enum class AccessSocketOperationType(val key: String) {
    DATABASE("database"),
    NETWORK_BROADCAST("network_broadcast"),
    HISTORY("history"),
    CRYPTO("crypto"),
    REGISTRATION("registration")
}
