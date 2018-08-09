package com.pixelplex.echoframework.model.operations

import com.google.common.primitives.Bytes
import com.google.common.primitives.UnsignedLong
import com.google.gson.*
import com.pixelplex.echoframework.model.*
import java.lang.reflect.Type

/**
 * Encapsulates asset creation information
 *
 * @author Dmitriy Bushuev
 */
class CreateAssetOperation @JvmOverloads constructor(
    var asset: Asset,
    override var fee: AssetAmount = AssetAmount(UnsignedLong.ZERO)
) : BaseOperation(OperationType.ASSET_CREATE_OPERATION) {

    override fun toBytes(): ByteArray {
        val feeBytes = fee.toBytes()
        val assetBytes = asset.toBytes()
        val extensionsBytes = extensions.toBytes()
        return Bytes.concat(
            feeBytes,
            assetBytes,
            extensionsBytes
        )
    }

    override fun toJsonString(): String {
        val gson = GsonBuilder().registerTypeAdapter(
            TransferOperation::class.java,
            CreateAssetSerializer()
        ).create()
        return gson.toJson(this)
    }

    override fun toJsonObject(): JsonElement = JsonArray().apply {
        add(id)
        val jsonObject = JsonObject().apply {
            add(KEY_FEE, fee.toJsonObject())
            addProperty(ISSUER_KEY, asset.issuer)
            addProperty(SYMBOL_KEY, asset.symbol ?: "")
            addProperty(PRECISION_KEY, asset.precision)
            add(OPTIONS_KEY, asset.assetOptions?.toJsonObject())
            if (asset.getBtsOptions() != null)
                add(BITASSETS_OPTIONS_KEY, asset.getBtsOptions()!!.toJsonObject())
            addProperty(PREDICTION_MARKET_KEY, asset.predictionMarket)
            add(EXTENSIONS_KEY, extensions.toJsonObject())
        }
        add(jsonObject)
    }

    /**
     * Encapsulates logic of transfer json serialization
     */
    class CreateAssetSerializer : JsonSerializer<CreateAssetOperation> {

        override fun serialize(
            createAsset: CreateAssetOperation,
            type: Type,
            jsonSerializationContext: JsonSerializationContext
        ): JsonElement {
            val arrayRep = JsonArray()
            arrayRep.add(createAsset.id)
            arrayRep.add(createAsset.toJsonObject())
            return arrayRep
        }
    }

    /**
     * It will convert this data into a nice TransferOperation object.
     */
    class CreateAssetDeserializer : JsonDeserializer<CreateAssetOperation> {

        @Throws(JsonParseException::class)
        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type,
            context: JsonDeserializationContext
        ): CreateAssetOperation? {

            if (json == null || !json.isJsonObject) return null

            val jsonObject = json.asJsonObject

            val fee = context.deserialize<AssetAmount>(
                jsonObject.get(KEY_FEE),
                AssetAmount::class.java
            )

            val parsedIssuer = jsonObject.get(ISSUER_KEY).asString
            val parsedSymbol = jsonObject.get(SYMBOL_KEY).asString
            val parsedPrecision = jsonObject.get(PRECISION_KEY).asInt
            val parsedOptions = context.deserialize<AssetOptions>(
                jsonObject.get(OPTIONS_KEY),
                AssetOptions::class.java
            )
            val parsedBitassetOpts = context.deserialize<BitassetOptions>(
                jsonObject.get(BITASSETS_OPTIONS_KEY),
                BitassetOptions::class.java
            )
            val parsedPredictionMarket = jsonObject.get(PREDICTION_MARKET_KEY).asBoolean

            val asset = Asset(DEFAULT_ASSET_ID).apply {
                issuer = parsedIssuer
                symbol = parsedSymbol
                precision = parsedPrecision
                assetOptions = parsedOptions
                setBtsOptions(parsedBitassetOpts)
                predictionMarket = parsedPredictionMarket
            }

            return CreateAssetOperation(asset, fee)
        }
    }

    companion object {
        private const val ISSUER_KEY = "issuer"
        private const val SYMBOL_KEY = "symbol"
        private const val PRECISION_KEY = "precision"
        private const val OPTIONS_KEY = "common_options"
        private const val BITASSETS_OPTIONS_KEY = "bitasset_opts"
        private const val PREDICTION_MARKET_KEY = "is_prediction_market"
        private const val EXTENSIONS_KEY = "extensions"

        private const val DEFAULT_ASSET_ID = "1.3.1"
    }

}
