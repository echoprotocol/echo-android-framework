package org.echo.mobile.framework.model.socketoperations

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.model.Asset
import org.echo.mobile.framework.model.AssetAmount
import org.echo.mobile.framework.model.AssetOptions
import org.echo.mobile.framework.model.Price

/**
 * Get a list of assets by id.
 *
 * @param assetIds of the assets to retrieve
 * @return The assets corresponding to the provided [assetIds]
 *
 * @author Daria Pechkovskaya
 */
class GetAssetsSocketOperation(
    override val apiId: Int,
    val assetIds: Array<String>,
    callId: Int,
    callback: Callback<List<Asset>>
) : SocketOperation<List<Asset>>(
    SocketMethodType.CALL,
    callId,
    listOf<Asset>().javaClass,
    callback
) {

    @Suppress("UNUSED_EXPRESSION")
    override fun createParameters(): JsonElement =
        JsonArray().apply {
            add(apiId)
            add(SocketOperationKeys.ASSETS.key)

            val assetsJson = JsonArray()
            assetIds.forEach { item -> assetsJson.add(item) }
            add(JsonArray().apply { add(assetsJson) })
        }

    override fun fromJson(json: String): List<Asset> {
        val parser = JsonParser()
        val jsonTree = parser.parse(json)

        val result = jsonTree.asJsonObject.get(RESULT_KEY)?.asJsonArray
            ?: return emptyList()

        val gson = GsonBuilder().registerTypeAdapter(
            AssetOptions::class.java,
            AssetOptions.AssetOptionsDeserializer()
        ).registerTypeAdapter(
            AssetAmount::class.java,
            AssetAmount.Deserializer()
        ).registerTypeAdapter(
            Price::class.java,
            Price.PriceDeserializer()
        ).registerTypeAdapter(
            Asset::class.java,
            Asset.AssetDeserializer()
        ).create()

        return gson.fromJson<List<Asset>>(result, object : TypeToken<List<Asset>>() {}.type)
    }

    companion object {
        private const val RESULT_KEY = "result"
    }

}
