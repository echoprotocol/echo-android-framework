package org.echo.mobile.framework.model

import com.google.common.primitives.Bytes
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import org.echo.mobile.framework.support.serialize
import java.lang.reflect.Type

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

    val bitassetOptions = Optional<BitassetOptions>(null, true)

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
        val symbolBytes = symbol!!.serialize()
        val precisionBytes = byteArrayOf(precision.toByte())
        val optionsBytes = assetOptions!!.toBytes()
        val btsOptionsBytes = bitassetOptions.toBytes()
        val predictionMarketBytes = predictionMarket.serialize()
        return Bytes.concat(
            issuerBytes, symbolBytes, precisionBytes, optionsBytes,
            btsOptionsBytes, predictionMarketBytes
        )
    }

    override fun hashCode() = getObjectId().hashCode()

    override fun equals(other: Any?) = if (other is Asset) {
        this.getObjectId() == other.getObjectId()
    } else {
        false
    }

    /**
     * Json deserializer for [Asset]
     */
    class AssetDeserializer : JsonDeserializer<Asset> {

        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext
        ): Asset? {
            if (json == null || !json.isJsonObject) return null

            val jsonOptions = json.asJsonObject

            val parsedId = jsonOptions.get(KEY_ID).asString
            val parsedSymbol = jsonOptions.get(SYMBOL_KEY)?.asString
            val parsedPrecision = jsonOptions.get(PRECISION_KEY)?.asInt ?: -1
            val issuerId = jsonOptions.get(ISSUER_KEY).asString
            val parsedIssuer = Account(issuerId)
            val parsedDynamicAssetDataId = jsonOptions.get(DYNAMIC_ASSET_DATA_ID_KEY)?.asString
            val parsedAssetOptions = context.deserialize<AssetOptions>(
                jsonOptions.get(OPTIONS_KEY),
                AssetOptions::class.java
            )
            val parsedBitassetOptions = context.deserialize<BitassetOptions>(
                jsonOptions.get(BITASSETS_OPTIONS_KEY),
                BitassetOptions::class.java
            )
            val parsedPredictionMarket = jsonOptions.get(PREDICTION_MARKET_KEY)?.asBoolean ?: false
            val parsedBitAssetId = jsonOptions.get(BITASSET_DATA_ID_KEY)?.asString

            return Asset(parsedId).apply {
                this.symbol = parsedSymbol
                this.precision = parsedPrecision
                this.issuer = parsedIssuer
                this.dynamicAssetDataId = parsedDynamicAssetDataId
                this.assetOptions = parsedAssetOptions
                this.setBtsOptions(parsedBitassetOptions)
                this.predictionMarket = parsedPredictionMarket
                this.bitAssetId = parsedBitAssetId
            }
        }
    }

    companion object {
       const val ISSUER_KEY = "issuer"
       const val PRECISION_KEY = "precision"
       const val SYMBOL_KEY = "symbol"
       const val DYNAMIC_ASSET_DATA_ID_KEY = "dynamic_asset_data_id"
       const val OPTIONS_KEY = "options"
       const val BITASSET_DATA_ID_KEY = "bitasset_data_id"
       const val BITASSETS_OPTIONS_KEY = "bitasset_opts"
       const val PREDICTION_MARKET_KEY = "is_prediction_market"
    }

}
