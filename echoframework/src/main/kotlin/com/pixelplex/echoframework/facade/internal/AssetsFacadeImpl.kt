package com.pixelplex.echoframework.facade.internal

import com.pixelplex.echoframework.Callback
import com.pixelplex.echoframework.facade.AssetsFacade
import com.pixelplex.echoframework.model.Asset
import com.pixelplex.echoframework.service.DatabaseApiService

/**
 * Implementation of [AssetsFacade]
 *
 * @author Dmitriy Bushuev
 */
class AssetsFacadeImpl(private val databaseApiService: DatabaseApiService) : AssetsFacade {

    override fun listAssets(lowerBound: String, limit: Int, callback: Callback<List<Asset>>) =
        databaseApiService.listAssets(lowerBound, limit, callback)

    override fun getAssets(assetIds: List<String>, callback: Callback<List<Asset>>) {
        databaseApiService.getAssets(assetIds, callback)
    }

}
