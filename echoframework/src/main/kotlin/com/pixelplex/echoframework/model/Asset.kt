package com.pixelplex.echoframework.model

import com.google.gson.annotations.SerializedName

/**
 * Class used to represent a specific asset on the Graphene platform
 *
 * @author Dmitriy Bushuev
 */
class Asset(id: String) : GrapheneObject(id), ByteSerializable {

    var symbol: String? = null

    var precision = -1

    var issuer: Account? = null

    @SerializedName(DYNAMIC_ASSET_DATA_ID_KEY)
    var dynamicAssetDataId: String? = null

    @SerializedName(OPTIONS_KEY)
    var assetOptions: AssetOptions? = null

    var bitassetOptions = Optional<BitassetOptions>(null)

    var predictionMarket = false

    @SerializedName(BITASSET_DATA_ID_KEY)
    var bitAssetId: String? = null

    /**
     * Asset copy constructor
     */
    constructor(asset: Asset) : this(asset.getObjectId()) {
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
        val issuerBytes = issuer!!.toBytes()
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

    companion object {
        private const val DYNAMIC_ASSET_DATA_ID_KEY = "dynamic_asset_data_id"
        private const val OPTIONS_KEY = "options"
        private const val BITASSET_DATA_ID_KEY = "bitasset_data_id"
    }

}
