package org.echo.mobile.framework.model.socketoperations

import com.google.common.reflect.TypeToken
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.model.contract.ContractInfo

/**
 * Retrieves information about selected contracts.
 *
 * @param contractIds List of contracts ids for retrieving information
 *
 * @author Daria Pechkovskaya
 */
class GetContractsSocketOperation(
    override val apiId: Int,
    private val contractIds: List<String>,
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
            add(SocketOperationKeys.GET_CONTRACTS.key)

            val idsJson = JsonArray().apply {
                contractIds.forEach { item -> this.add(item) }
            }

            add(JsonArray().apply { add(idsJson) })
        }

    override fun fromJson(json: String): List<ContractInfo> {
        val parser = JsonParser()
        val jsonTree = parser.parse(json)

        try {
            val result = jsonTree.asJsonObject.get(RESULT_KEY)?.asJsonArray

            return GsonBuilder().create().fromJson<List<ContractInfo>>(
                result,
                object : TypeToken<List<ContractInfo>>() {}.type
            )

        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return listOf()
    }
}
