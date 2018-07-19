package com.pixelplex.echolib.model.socketoperations

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.pixelplex.echolib.Callback
import com.pixelplex.echolib.ILLEGAL_ID
import com.pixelplex.echolib.model.Asset
import com.pixelplex.echolib.model.AssetAmount
import com.pixelplex.echolib.model.BaseOperation
import com.pixelplex.echolib.support.Api
import com.pixelplex.echolib.support.getId

/**
 * Represents blockchain call. Returns list of [AssetAmount] for [operations]
 *
 * @author Daria Pechkovskaya
 */
class RequiredFeesSocketOperation(
    val operations: List<BaseOperation>,
    val asset: Asset,
    val api: Api,
    method: SocketMethodType = SocketMethodType.CALL,
    callback: Callback<List<AssetAmount>>
) : SocketOperation<List<AssetAmount>>(
    method,
    ILLEGAL_ID,
    listOf<AssetAmount>().javaClass,
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

    override val apiId: Int
        get() = api.getId()

    override fun fromJson(json: String): List<AssetAmount>? {
        val parser = JsonParser()
        val jsonTree = parser.parse(json)

        try {
            val result = jsonTree.asJsonObject.get("result")?.asJsonArray

            val gson = GsonBuilder().registerTypeAdapter(
                AssetAmount::class.java,
                AssetAmount.Deserializer()
            ).create()

            return gson.fromJson<List<AssetAmount>>(
                result,
                object : TypeToken<List<AssetAmount>>() {}.type
            )

        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return arrayListOf()
    }
}
