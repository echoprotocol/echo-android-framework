package com.pixelplex.echoframework.model

import com.google.common.primitives.UnsignedLong
import com.google.gson.*
import com.google.gson.annotations.SerializedName
import com.pixelplex.bitcoinj.revert
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
    var marketFeePercent: Long = 0,
    @SerializedName("max_market_fee")
    var maxMarketFee: UnsignedLong? = null,
    @SerializedName("issuer_permissions")
    var issuerPermissions: Long = 0,
    var flags: Int = 0,
    @SerializedName("core_exchange_rate")
    var coreExchangeRate: Price? = null,
    var description: String? = null
) : JsonSerializable, ByteSerializable {

    override fun toBytes(): ByteArray {
        val maxSupplyBytes = maxSupply!!.toLong().revert()
        val marketFeePercentBytes = marketFeePercent.toShort().revert()
        val maxMarketFeeBytes = maxMarketFee!!.toLong().revert()
        val issuerPermissionsBytes = issuerPermissions.toShort().revert()
        val flagsBytes = flags.toShort().revert()
        val coreExchangeRateBytes = coreExchangeRate!!.toBytes()
        val descriptionBytes = description?.toByteArray() ?: ByteArray(0)
        val extensionsBytes = ByteArray(1)
        return maxSupplyBytes + marketFeePercentBytes + maxMarketFeeBytes + issuerPermissionsBytes +
                flagsBytes + coreExchangeRateBytes +
                byteArrayOf(0) + byteArrayOf(0) +
                byteArrayOf(0) + byteArrayOf(0) + descriptionBytes + extensionsBytes
    }

    override fun toJsonString(): String? = null

    override fun toJsonObject(): JsonElement? = JsonObject().apply {
        addProperty(MAX_SUPPLY_KEY, maxSupply?.toLong() ?: 0)
        addProperty(MARKET_FEE_PERCENT_KEY, marketFeePercent.toShort())
        addProperty(MAX_MARKET_FEE_KEY, maxMarketFee?.toLong() ?: 0)
        addProperty(ISSUER_PERMISSION_KEY, issuerPermissions)
        addProperty(FLAGS_KEY, flags)
        add(CORE_EXCHANGE_RATE_KEY, coreExchangeRate?.toJsonObject())
        add(WHITELIST_KEY, JsonArray())
        add(BLACKLIST_KEY, JsonArray())
        add(WHITELIST_MARKETS_KEY, JsonArray())
        add(BLACKLIST_MARKETS_KEY, JsonArray())
        add(EXTENSIONS_KEY, JsonArray())
        addProperty(DESCRIPTION_KEY, description ?: "")
    }

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
            val marketFeePercent = jsonOptions.get(MARKET_FEE_PERCENT_KEY).asLong
            val maxMarketFee = UnsignedLong.valueOf(jsonOptions.get(MAX_MARKET_FEE_KEY).asLong)
            val flags = jsonOptions.get(FLAGS_KEY).asInt
            val issuerPermissions = jsonOptions.get(ISSUER_PERMISSION_KEY).asLong
            val description = jsonOptions.get(DESCRIPTION_KEY).asString
            val coreExchangeRate = context.deserialize<Price>(
                jsonOptions.get(CORE_EXCHANGE_RATE_KEY),
                Price::class.java
            )

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
        private const val EXTENSIONS_KEY = "extensions"
        private const val WHITELIST_KEY = "whitelist_authorities"
        private const val BLACKLIST_KEY = "blacklist_authorities"
        private const val WHITELIST_MARKETS_KEY = "whitelist_markets"
        private const val BLACKLIST_MARKETS_KEY = "blacklist_markets"

    }

}
