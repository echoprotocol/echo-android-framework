package org.echo.mobile.framework.model.socketoperations

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import org.echo.mobile.framework.Callback

/**
 * Checks whether [contractId] is ERC20 token
 *
 * @author Dmitriy Bushuev
 */
class CheckERC20TokenSocketOperation(
    override val apiId: Int,
    private val contractId: String,
    callId: Int,
    callback: Callback<Boolean>
) : SocketOperation<Boolean>(
    SocketMethodType.CALL,
    callId,
    Boolean::class.java,
    callback
) {

    override fun createParameters(): JsonElement =
        JsonArray().apply {
            add(apiId)
            add(SocketOperationKeys.CHECK_ERC20_TOKEN.key)
            add(JsonArray().apply { add(contractId) })
        }

    override fun fromJson(json: String): Boolean {
        val parser = JsonParser()
        val jsonTree = parser.parse(json)

        if (!jsonTree.isJsonObject || jsonTree.asJsonObject.get(RESULT_KEY) == null) {
            return false
        }

        try {
            return jsonTree.asJsonObject.get(RESULT_KEY)!!.asBoolean
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return false
    }

}
