package com.pixelplex.echoframework.facade

import com.pixelplex.echoframework.Callback
import com.pixelplex.echoframework.model.Asset
import com.pixelplex.echoframework.model.BitassetOptions

/**
 * Encapsulates logic, associated with echo blockchain assets use cases
 *
 * @author Dmitriy Bushuev
 */
interface AssetsFacade {

    /**
     * Creates asset [asset] with required parameters
     */
    fun createAsset(
        name: String,
        password: String,
        asset: Asset,
        bitassetOptions: BitassetOptions?,
        predictionMarket: Boolean,
        callback: Callback<Boolean>
    )

    /**
     * Query list of assets by required asset symbol [lowerBound] with limit [limit]
     */
    fun listAssets(lowerBound: String, limit: Int, callback: Callback<List<Asset>>)

    /**
     * Query list of assets by it's ids [assetIds]
     */
    fun getAssets(assetIds: List<String>, callback: Callback<List<Asset>>)

}
