package com.pixelplex.echoframework.model

import com.google.common.primitives.UnsignedLong
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type

/**
 * Contains options available on all assets in the network
 * [AssetOptions details](https://dev-doc.myecho.app/structgraphene_1_1chain_1_1asset__options.html)
 *
 * @author Dmitriy Bushuev
 */
class AssetOptions(
    @SerializedName("max_supply")
    var maxSupply: UnsignedLong? = null,
    @SerializedName("market_fee_percent")
    var marketFeePercent: Float = 0.toFloat(),
    @SerializedName("max_market_fee")
    var maxMarketFee: UnsignedLong? = null,
    @SerializedName("issuer_permissions")
    var issuerPermissions: Long = 0,
    var flags: Int = 0,
    @SerializedName("core_exchange_rate")
    var coreExchangeRate: Price? = null,
    var description: String? = null
) {

    /**
     * Json deserializer for [AssetOptions]
     */
    class AssetOptionsDeserializer : JsonDeserializer<AssetOptions> {

        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext
        ): AssetOptions? {
            if (json == null || !json.isJsonObject) return null

            val jsonOptions = json.asJsonObject

            val maxSupply = UnsignedLong.valueOf(jsonOptions.get(MAX_SUPPLY_KEY).asLong)
            val marketFeePercent = jsonOptions.get(MARKET_FEE_PERCENT_KEY).asFloat
            val maxMarketFee = UnsignedLong.valueOf(jsonOptions.get(MAX_MARKET_FEE_KEY).asLong)
            val flags = jsonOptions.get(FLAGS_KEY).asInt
            val issuerPermissions = jsonOptions.get(ISSUER_PERMISSION_KEY).asLong
            val description = jsonOptions.get(DESCRIPTION_KEY).asString
            val coreExchangeRate = context.deserialize<Price>(jsonOptions, Price::class.java)

            return AssetOptions(
                maxSupply,
                marketFeePercent,
                maxMarketFee,
                issuerPermissions,
                flags,
                coreExchangeRate,
                description
            )
        }

    }

    companion object {
        private const val MAX_SUPPLY_KEY = "max_supply"
        private const val MARKET_FEE_PERCENT_KEY = "market_fee_percent"
        private const val MAX_MARKET_FEE_KEY = "max_market_fee"
        private const val FLAGS_KEY = "flags"
        private const val ISSUER_PERMISSION_KEY = "issuer_permissions"
        private const val CORE_EXCHANGE_RATE_KEY = "core_exchange_rate"
        private const val DESCRIPTION_KEY = "description"

    }

}
