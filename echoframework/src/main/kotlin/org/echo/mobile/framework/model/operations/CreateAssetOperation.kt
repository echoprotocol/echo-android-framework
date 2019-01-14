package org.echo.mobile.framework.model.operations

import com.google.common.primitives.Bytes
import com.google.common.primitives.UnsignedLong
import com.google.gson.*
import org.echo.mobile.framework.model.*
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
        return Bytes.concat(feeBytes, assetBytes, extensionsBytes)
    }

    override fun toJsonString(): String {
        val gson = GsonBuilder().registerTypeAdapter(
            CreateAssetOperation::class.java, CreateAssetSerializer()
        ).create()
        return gson.toJson(this)
    }

    override fun toJsonObject(): JsonElement = JsonArray().apply {
        add(id)
        val jsonObject = JsonObject().apply {
            add(KEY_FEE, fee.toJsonObject())
            addProperty(Asset.ISSUER_KEY, asset.issuer!!.getObjectId())
            addProperty(Asset.SYMBOL_KEY, asset.symbol ?: "")
            addProperty(Asset.PRECISION_KEY, asset.precision)
            add(OPTIONS_KEY, asset.assetOptions?.toJsonObject())
            if (asset.getBtsOptions() != null)
                add(Asset.BITASSETS_OPTIONS_KEY, asset.getBtsOptions()!!.toJsonObject())
            addProperty(Asset.PREDICTION_MARKET_KEY, asset.predictionMarket)
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

            val parsedIssuerId = jsonObject.get(Asset.ISSUER_KEY).asString
            val issuerAccount = Account(parsedIssuerId)

            val parsedSymbol = jsonObject.get(Asset.SYMBOL_KEY).asString
            val parsedPrecision = jsonObject.get(Asset.PRECISION_KEY).asInt
            val parsedOptions = context.deserialize<AssetOptions>(
                jsonObject.get(OPTIONS_KEY),
                AssetOptions::class.java
            )
            val parsedBitassetOpts = context.deserialize<BitassetOptions>(
                jsonObject.get(Asset.BITASSETS_OPTIONS_KEY),
                BitassetOptions::class.java
            )
            val parsedPredictionMarket = jsonObject.get(Asset.PREDICTION_MARKET_KEY).asBoolean

            val asset = Asset(DEFAULT_ASSET_ID).apply {
                issuer = issuerAccount
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
        private const val OPTIONS_KEY = "common_options"
        private const val EXTENSIONS_KEY = "extensions"
        private const val DEFAULT_ASSET_ID = "1.3.0"
    }

}
