package org.echo.mobile.framework.model.socketoperations

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import org.echo.mobile.framework.Callback

/**
 * Retrieves full information about contract.
 *
 * @param contractId Id of contract for retrieving information
 *
 * @author Daria Pechkovskaya
 */
class GetContractSocketOperation(
    override val apiId: Int,
    private val contractId: String,
    callId: Int,
    callback: Callback<String>
) : SocketOperation<String>(
    SocketMethodType.CALL,
    callId,
    String::class.java,
    callback
) {

    override fun createParameters(): JsonElement =
        JsonArray().apply {
            add(apiId)
            add(SocketOperationKeys.GET_CONTRACT.key)
            add(JsonArray().apply { add(contractId) })
        }

    override fun fromJson(json: String): String? {
        val parser = JsonParser()
        val jsonTree = parser.parse(json)

        if (!jsonTree.isJsonObject || jsonTree.asJsonObject.get(RESULT_KEY) == null) {
            return null
        }

        try {
            val result = jsonTree.asJsonObject.get(RESULT_KEY)!!.asJsonArray

            return result[1]?.asJsonObject?.get(CONTRACT_CODE_KEY)?.asString
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return null
    }

    companion object {
        private const val CONTRACT_CODE_KEY = "code"
    }
}
