package org.echo.mobile.framework.model.socketoperations

import com.google.common.reflect.TypeToken
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.model.contract.ContractInfo

/**
 * Retrieves information about all existing contracts.
 *
 * @author Daria Pechkovskaya
 */
class GetAllContractsSocketOperation(
    override val apiId: Int,
    callId: Int,
    callback: Callback<List<ContractInfo>>
) : SocketOperation<List<ContractInfo>>(
    SocketMethodType.CALL,
    callId,
    listOf<ContractInfo>().javaClass,
    callback
) {

    override fun createParameters(): JsonElement =
        JsonArray().apply {
            add(apiId)
            add(SocketOperationKeys.GET_ALL_CONTRACTS.key)
            add(JsonArray())
        }

    override fun fromJson(json: String): List<ContractInfo> {
        val parser = JsonParser()
        val jsonTree = parser.parse(json)

        try {
            val result = jsonTree.asJsonObject.get(RESULT_KEY)?.asJsonArray

            return GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
                .fromJson<List<ContractInfo>>(
                    result,
                    object : TypeToken<List<ContractInfo>>() {}.type
                )

        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return emptyList()
    }
}
