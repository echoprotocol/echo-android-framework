package com.pixelplex.echoframework.facade

import com.pixelplex.echoframework.Callback
import com.pixelplex.echoframework.model.Asset

/**
 * Encapsulates logic, associated with echo blockchain assets use cases
 *
 * @author Dmitriy Bushuev
 */
interface AssetsFacade {

    /**
     * Query list of assets by required asset symbol [lowerBound] with limit [limit]
     */
    fun listAssets(lowerBound: String, limit: Int, callback: Callback<List<Asset>>)

}
