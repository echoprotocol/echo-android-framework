package org.echo.mobile.framework.facade

import org.echo.mobile.framework.Callback
import org.echo.mobile.framework.model.Asset
import org.echo.mobile.framework.model.socketoperations.TransactionResultCallback

/**
 * Encapsulates logic, associated with echo blockchain assets use cases
 *
 * @author Dmitriy Bushuev
 */
interface AssetsFacade {

    /**
     * Creates [asset] with required parameters using [name] account's [wif] for transaction signing
     *
     * @param broadcastCallback Callback for result of operation broadcast
     * @param resultCallback Callback for retrieving result of operation
     */
    fun createAsset(
        name: String,
        wif: String,
        asset: Asset,
        broadcastCallback: Callback<Boolean>,
        resultCallback: TransactionResultCallback
    )

    /**
     * Issues [asset] from [issuerNameOrId] account to [destinationIdOrName] account using source
     * account [wif] for signature
     */
    fun issueAsset(
        issuerNameOrId: String,
        wif: String,
        asset: String,
        amount: String,
        destinationIdOrName: String,
        broadcastCallback: Callback<Boolean>,
        resultCallback: TransactionResultCallback
    )

    /**
     * Query list of assets by required asset symbol [lowerBound] with limit [limit]
     */
    fun listAssets(lowerBound: String, limit: Int, callback: Callback<List<Asset>>)

    /**
     * Query list of assets by it's ids [assetIds]
     */
    fun getAssets(assetIds: List<String>, callback: Callback<List<Asset>>)

    /**
     * Query list of assets by it's [symbolsOrIds]
     */
    fun lookupAssetsSymbols(symbolsOrIds: List<String>, callback: Callback<List<Asset>>)

}
