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
    bitassetOptions: BitassetOptions? = null,
    var predictionMarket: Boolean,
    override var fee: AssetAmount = AssetAmount(UnsignedLong.ZERO)
) : BaseOperation(OperationType.ASSET_CREATE_OPERATION) {

    private val bitassetOptions = Optional(bitassetOptions)

    override fun toBytes(): ByteArray {
        val feeBytes = fee.toBytes()
        val assetBytes = asset.toBytes()
        val btsOptionsBytes = bitassetOptions.toBytes()
        val predictionMarketBytes =
            if (predictionMarket) byteArrayOf(1.toByte()) else byteArrayOf(0.toByte())
        val extensionsBytes = extensions.toBytes()
        return Bytes.concat(
            feeBytes,
            assetBytes,
            btsOptionsBytes,

            predictionMarketBytes,
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
            if (bitassetOptions.isSet)
                add(BITASSETS_OPTIONS_KEY, bitassetOptions.toJsonObject())
            addProperty(PREDICTION_MARKET_KEY, predictionMarket)
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

    companion object {
        private const val ISSUER_KEY = "issuer"
        private const val SYMBOL_KEY = "symbol"
        private const val PRECISION_KEY = "precision"
        private const val OPTIONS_KEY = "common_options"
        private const val BITASSETS_OPTIONS_KEY = "bitasset_opts"
        private const val PREDICTION_MARKET_KEY = "is_prediction_market"
        private const val EXTENSIONS_KEY = "extensions"
    }

}
