package com.pixelplex.echoframework.model.socketoperations

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.pixelplex.echoframework.Callback
import com.pixelplex.echoframework.model.contract.ContractInfo
import com.pixelplex.echoframework.model.contract.ContractStruct

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
            val result = jsonTree.asJsonObject.get(RESULT_KEY)!!.asJsonObject

            val gson = Gson()

            val contractInfo = gson.fromJson<ContractInfo>(
                result.get(ContractStruct.KEY_CONTRACT_INFO),
                ContractInfo::class.java
            )
            val code = result.get(ContractStruct.KEY_CODE).asJsonPrimitive.asString

            val storage = result.get(ContractStruct.KEY_STORAGE).asJsonArray
            val storageMap = hashMapOf<String, String>()

            val size = storage?.size() ?: 0
            for (i in 0 until size) {
                val subArray = storage!!.get(i).asJsonArray
                val id = subArray.get(0).asString
                val value = subArray.get(1).asString
                value?.let { nonNullValue ->
                    storageMap[id] = nonNullValue
                }
            }

            return ContractStruct(contractInfo, code, storageMap)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return null
    }
}
