package com.pixelplex.echolib.model.socketoperations

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.pixelplex.echolib.model.Asset

/**
 * Get a list of assets by id.
 *
 * @param assetIds of the assets to retrieve
 * @return The assets corresponding to the provided [assetIds]
 *
 * @author Daria Pechkovskaya
 */
class GetAssetsSocketOperation(
    method: SocketMethodType = SocketMethodType.CALL,
    callId: Int,
    apiId: Int,
    result: OperationResult<List<Asset>>,
    val assetIds: Array<String>
) : SocketOperation(method, callId, apiId, result) {

    @Suppress("UNUSED_EXPRESSION")
    override fun createParameters(): JsonElement =
        JsonArray().apply {
            add(apiId)
            add(SocketOperationKeys.ASSETS.key)

            val assetsJson = JsonArray()
            assetIds.forEach { item -> assetsJson.add(item) }
            add(JsonArray().apply { assetsJson })
        }
}
