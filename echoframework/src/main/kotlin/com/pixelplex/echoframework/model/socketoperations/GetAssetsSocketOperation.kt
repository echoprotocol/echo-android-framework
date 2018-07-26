package com.pixelplex.echoframework.model.socketoperations

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.pixelplex.echoframework.Callback
import com.pixelplex.echoframework.ILLEGAL_ID
import com.pixelplex.echoframework.model.Asset

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
    method: SocketMethodType = SocketMethodType.CALL,
    callback: Callback<List<Asset>>

) : SocketOperation<List<Asset>>(method, ILLEGAL_ID, listOf<Asset>().javaClass, callback) {

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

        val result = jsonTree.asJsonObject.get(RESULT_KEY)?.asJsonObject
                ?: return emptyList()

        val gson = GsonBuilder().create()

        return gson.fromJson<List<Asset>>(result, object : TypeToken<List<Asset>>() {}.type)
    }

    companion object {
        private const val RESULT_KEY = "result"
    }

}
