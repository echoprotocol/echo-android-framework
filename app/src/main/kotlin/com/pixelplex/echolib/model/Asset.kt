package com.pixelplex.echolib.model

/**
 * Class used to represent a specific asset on the Graphene platform
 *
 * @author Dmitriy Bushuev
 */
class Asset : GrapheneObject {

    var symbol: String? = null

    var precision = -1

    var issuer: String? = null

    var description: String? = null

    var dynamicAssetDataId: String? = null

    var assetOptions: AssetOptions? = null

    var bitAssetId: String? = null

    var assetType: AssetType? = null

    constructor(id: String) : super(id)

    constructor(id: String, symbol: String, precision: Int) : super(id) {
        this.symbol = symbol
        this.precision = precision
    }

    constructor(id: String, symbol: String, precision: Int, issuer: String) : super(id) {
        this.symbol = symbol
        this.precision = precision
        this.issuer = issuer
    }

    /**
     * Asset copy constructor
     */
    constructor(asset: Asset) : super(asset.getObjectId()) {
        this.symbol = asset.symbol
        this.precision = asset.precision
        this.issuer = asset.issuer
        this.description = asset.description
        this.dynamicAssetDataId = asset.dynamicAssetDataId
        this.assetOptions = asset.assetOptions
        this.bitAssetId = asset.bitAssetId
        this.assetType = asset.assetType
    }

    override fun hashCode() = getObjectId().hashCode()

    override fun equals(other: Any?) = if (other is Asset) {
        this.getObjectId() == other.getObjectId()
    } else {
        false
    }

    /**
     * Enum type used to represent the possible types an asset can be classified into.
     */
    enum class AssetType {
        CORE_ASSET,
        UIA,
        SMART_COIN,
        PREDICTION_MARKET
    }

}
