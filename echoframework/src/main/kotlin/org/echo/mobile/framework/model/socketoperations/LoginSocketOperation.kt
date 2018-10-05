package org.echo.mobile.framework.model.socketoperations

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.ILLEGAL_ID

/**
 * Represents blockchain call for access to blockchain
 *
 * @author Daria Pechkovskaya
 */
class LoginSocketOperation(
    override val apiId: Int,
    callId: Int,
    callback: Callback<Boolean>
) : SocketOperation<Boolean>(SocketMethodType.CALL, callId, Boolean::class.java, callback) {

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

        if (!jsonTree.isJsonObject || jsonTree.asJsonObject.get(RESULT_KEY) == null) {
            return null
        }

        return jsonTree.asJsonObject.get(RESULT_KEY)?.asJsonPrimitive?.asBoolean
    }
}
