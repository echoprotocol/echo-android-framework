package com.pixelplex.echoframework.model

import com.google.common.primitives.Bytes
import com.google.common.primitives.UnsignedLong
import com.google.gson.*
import com.google.gson.annotations.SerializedName
import com.pixelplex.echoframework.support.Int64
import com.pixelplex.echoframework.support.Uint16
import com.pixelplex.echoframework.support.serialize
import java.lang.reflect.Type

/**
 * Contains options available on all assets in the network
 * [AssetOptions details](https://dev-doc.myecho.app/structgraphene_1_1chain_1_1asset__options.html)
 *
 * @author Dmitriy Bushuev
 */
class AssetOptions @JvmOverloads constructor(
    @SerializedName(MAX_SUPPLY_KEY)
    var maxSupply: UnsignedLong? = null,
    @SerializedName(MARKET_FEE_PERCENT_KEY)
    var marketFeePercent: Long = 0,
    @SerializedName(MAX_MARKET_FEE_KEY)
    var maxMarketFee: UnsignedLong? = null,
    @SerializedName(ISSUER_PERMISSION_KEY)
    var issuerPermissions: Long = 0,
    var flags: Int = 0,
    @SerializedName(CORE_EXCHANGE_RATE_KEY)
    var coreExchangeRate: Price? = null,
    var description: String? = null
) : JsonSerializable, ByteSerializable {

    var whitelistAuthorities: Set<String> = setOf()
    var blacklistAuthorities: Set<String> = setOf()
    var whitelistMarkets: Set<String> = setOf()
    var blacklistMarkets: Set<String> = setOf()

    var extensions = Extensions()

    override fun toBytes(): ByteArray {
        val maxSupplyBytes = Int64.serialize(maxSupply!!)
        val marketFeePercentBytes = Uint16.serialize(marketFeePercent)
        val maxMarketFeeBytes = Int64.serialize(maxMarketFee!!)
        val issuerPermissionsBytes = Uint16.serialize(issuerPermissions)
        val flagsBytes = Uint16.serialize(flags)
        val coreExchangeRateBytes = coreExchangeRate!!.toBytes()

        val whitelistAuthoritiesBytes = whitelistAuthorities.serialize { auth ->
            Account(auth).toBytes()
        }

        val blacklistAuthoritiesBytes = blacklistAuthorities.serialize { auth ->
            Account(auth).toBytes()
        }

        val whitelistMarketsBytes = whitelistMarkets.serialize { market ->
            Account(market).toBytes()
        }

        val blacklistMarketsBytes = blacklistMarkets.serialize { market ->
            Account(market).toBytes()
        }

        val descriptionBytes = description?.serialize() ?: byteArrayOf(0)
        val extensionsBytes = extensions.toBytes()

        return Bytes.concat(
            maxSupplyBytes,
            marketFeePercentBytes,
            maxMarketFeeBytes,
            issuerPermissionsBytes,
            flagsBytes,
            coreExchangeRateBytes,
            whitelistAuthoritiesBytes,
            blacklistAuthoritiesBytes,
            whitelistMarketsBytes,
            blacklistMarketsBytes,
            descriptionBytes,
            extensionsBytes
        )
    }

    override fun toJsonString(): String? = toJsonObject().toString()

    override fun toJsonObject(): JsonElement? = JsonObject().apply {
        addProperty(MAX_SUPPLY_KEY, maxSupply?.toLong() ?: 0)
        addProperty(MARKET_FEE_PERCENT_KEY, marketFeePercent.toShort())
        addProperty(MAX_MARKET_FEE_KEY, maxMarketFee?.toLong() ?: 0)
        addProperty(ISSUER_PERMISSION_KEY, issuerPermissions)
        addProperty(FLAGS_KEY, flags)
        add(CORE_EXCHANGE_RATE_KEY, coreExchangeRate?.toJsonObject())
        add(WHITELIST_KEY, JsonArray().apply { whitelistAuthorities.forEach { add(it) } })
        add(BLACKLIST_KEY, JsonArray().apply { blacklistAuthorities.forEach { add(it) } })
        add(WHITELIST_MARKETS_KEY, JsonArray().apply { whitelistMarkets.forEach { add(it) } })
        add(BLACKLIST_MARKETS_KEY, JsonArray().apply { blacklistMarkets.forEach { add(it) } })
        addProperty(DESCRIPTION_KEY, description ?: "")
        add(EXTENSIONS_KEY, extensions.toJsonObject())
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

        val CHARGE_MARKET_FEE = 0x01
        val WHITE_LIST = 0x02
        val OVERRIDE_AUTHORITY = 0x04
        val TRANSFER_RESTRICTED = 0x08
        val DISABLE_FORCE_SETTLE = 0x10
        val GLOBAL_SETTLE = 0x20
        val DISABLE_CONFIDENTIAL = 0x40
        val WITNESS_FED_ASSET = 0x80
        val COMITEE_FED_ASSET = 0x100
    }

}
