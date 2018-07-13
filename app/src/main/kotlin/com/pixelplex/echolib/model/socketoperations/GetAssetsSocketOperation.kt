package com.pixelplex.echolib.model.socketoperations

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.pixelplex.echolib.Callback
import com.pixelplex.echolib.model.Asset
import com.pixelplex.echolib.support.model.Api
import com.pixelplex.echolib.support.model.getId

/**
 * Get a list of assets by id.
 *
 * @param assetIds of the assets to retrieve
 * @return The assets corresponding to the provided [assetIds]
 *
 * @author Daria Pechkovskaya
 */
class GetAssetsSocketOperation(
    val api: Api,
    val assetIds: Array<String>,
    method: SocketMethodType = SocketMethodType.CALL,
    callId: Int,
    callback: Callback<List<Asset>>

) : SocketOperation<List<Asset>>(method, callId, listOf<Asset>().javaClass, callback) {

    @Suppress("UNUSED_EXPRESSION")
    override fun createParameters(): JsonElement =
        JsonArray().apply {
            add(apiId)
            add(SocketOperationKeys.ASSETS.key)

            val assetsJson = JsonArray()
            assetIds.forEach { item -> assetsJson.add(item) }
            add(JsonArray().apply { assetsJson })
        }

    override val apiId: Int
        get() = api.getId()
}
