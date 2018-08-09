package com.pixelplex.echoframework.model

import com.google.gson.annotations.SerializedName

/**
 * Class used to represent a specific asset on the Graphene platform
 *
 * @author Dmitriy Bushuev
 */
class Asset : GrapheneObject, ByteSerializable {

    var symbol: String? = null

    var precision = -1

    var issuer: String? = null

    @SerializedName("dynamic_asset_data_id")
    var dynamicAssetDataId: String? = null

    @SerializedName("options")
    var assetOptions: AssetOptions? = null

    var bitassetOptions = Optional<BitassetOptions>(null)

    var predictionMarket = false

    @SerializedName("bitasset_data_id")
    var bitAssetId: String? = null

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
        this.dynamicAssetDataId = asset.dynamicAssetDataId
        this.assetOptions = asset.assetOptions
        this.bitAssetId = asset.bitAssetId
    }

    /**
     * Set optional [bitassetOptions] field
     */
    fun setBtsOptions(btsOptions: BitassetOptions?) {
        bitassetOptions.field = btsOptions
    }

    /**
     * Get optional [bitassetOptions] field
     */
    fun getBtsOptions() = bitassetOptions.field

    override fun toBytes(): ByteArray {
        val issuerBytes = Account(issuer!!).toBytes()
        val symbolBytes = byteArrayOf(symbol!!.length.toByte()) + symbol!!.toByteArray()
        val precisionBytes = precision.toByte()
        val optionsBytes = assetOptions!!.toBytes()
        val btsOptionsBytes = bitassetOptions.toBytes()
        val predictionMarketBytes =
            if (predictionMarket) byteArrayOf(1.toByte()) else byteArrayOf(0.toByte())
        return issuerBytes + symbolBytes + precisionBytes + optionsBytes + btsOptionsBytes +
                predictionMarketBytes
    }

    override fun hashCode() = getObjectId().hashCode()

    override fun equals(other: Any?) = if (other is Asset) {
        this.getObjectId() == other.getObjectId()
    } else {
        false
    }

}
