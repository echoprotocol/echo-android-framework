package org.echo.mobile.framework.model.socketoperations

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.model.Asset
import org.echo.mobile.framework.model.AssetAmount
import org.echo.mobile.framework.model.BaseOperation
import org.echo.mobile.framework.model.contract.ContractFee

/**
 * Represents blockchain call. Returns list of fees required for contract call
 *
 * Fee is represented in [ContractFee] form
 *
 * @author Dmitriy Bushuev
 */
class RequiredContractFeesSocketOperation(
    override val apiId: Int,
    val operations: List<BaseOperation>,
    val asset: Asset,
    callId: Int,
    callback: Callback<List<ContractFee>>
) : SocketOperation<List<ContractFee>>(
    SocketMethodType.CALL,
    callId,
    listOf<ContractFee>().javaClass,
    callback
) {

    override fun createParameters(): JsonElement =
        JsonArray().apply {
            add(apiId)
            add(SocketOperationKeys.REQUIRED_FEES.key)
            add(JsonArray().apply {
                val operationsJson = JsonArray().apply {
                    operations.forEach { operation ->
                        add(operation.toJsonObject())
                    }
                }
                add(operationsJson)
                add(asset.getObjectId())
            })
        }

    override fun fromJson(json: String): List<ContractFee>? {
        val parser = JsonParser()
        val jsonTree = parser.parse(json)

        try {
            val result = jsonTree.asJsonObject.get(RESULT_KEY)?.asJsonArray

            return configureGson().fromJson<List<ContractFee>>(
                result,
                object : TypeToken<List<ContractFee>>() {}.type
            )

        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return arrayListOf()
    }

    private fun configureGson() = GsonBuilder().apply {
        registerTypeAdapter(AssetAmount::class.java, AssetAmount.Deserializer())
    }.create()
}
