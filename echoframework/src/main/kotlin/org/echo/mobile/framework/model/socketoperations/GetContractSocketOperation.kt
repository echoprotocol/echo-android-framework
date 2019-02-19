package org.echo.mobile.framework.model.socketoperations

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.model.contract.ContractStruct

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
    callback: Callback<ContractStruct>
) : SocketOperation<ContractStruct>(
    SocketMethodType.CALL,
    callId,
    ContractStruct::class.java,
    callback
) {

    override fun createParameters(): JsonElement =
        JsonArray().apply {
            add(apiId)
            add(SocketOperationKeys.GET_CONTRACT.key)
            add(JsonArray().apply { add(contractId) })
        }

    override fun fromJson(json: String): ContractStruct? {
        val parser = JsonParser()
        val jsonTree = parser.parse(json)

        if (!jsonTree.isJsonObject || jsonTree.asJsonObject.get(RESULT_KEY) == null) {
            return null
        }

        try {
            val result = jsonTree.asJsonObject.get(RESULT_KEY)!!.asJsonArray

            val contractType = result[0].asInt
            val contractCode = result[1]?.asJsonObject?.get(CONTRACT_CODE_KEY)?.asString ?: ""

            return ContractStruct(contractType, contractCode)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return null
    }

    companion object {
        private const val CONTRACT_CODE_KEY = "code"
    }
}
