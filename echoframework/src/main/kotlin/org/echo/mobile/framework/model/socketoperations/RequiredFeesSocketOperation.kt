package org.echo.mobile.framework.model.socketoperations

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.ILLEGAL_ID
import org.echo.mobile.framework.model.Asset
import org.echo.mobile.framework.model.AssetAmount
import org.echo.mobile.framework.model.BaseOperation

/**
 * Represents blockchain call. Returns list of [AssetAmount] for [operations]
 *
 * @author Daria Pechkovskaya
 */
class RequiredFeesSocketOperation(
    override val apiId: Int,
    val operations: List<BaseOperation>,
    val asset: Asset,
    callId: Int,
    callback: Callback<List<AssetAmount>>
) : SocketOperation<List<AssetAmount>>(
    SocketMethodType.CALL,
    callId,
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

    override fun fromJson(json: String): List<AssetAmount>? {
        val parser = JsonParser()
        val jsonTree = parser.parse(json)

        try {
            val result = jsonTree.asJsonObject.get(RESULT_KEY)?.asJsonArray

            return configureGson().fromJson<List<AssetAmount>>(
                result,
                object : TypeToken<List<AssetAmount>>() {}.type
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
