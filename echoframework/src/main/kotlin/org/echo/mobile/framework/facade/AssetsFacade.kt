package org.echo.mobile.framework.facade

import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.model.Asset

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
        callback: Callback<Boolean>
    )

    /**
     * Issues [asset] from [issuerNameOrId] account to [destinationIdOrName] account
     */
    fun issueAsset(
        issuerNameOrId: String,
        password: String,
        asset: String,
        amount: String,
        destinationIdOrName: String,
        message: String?,
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
