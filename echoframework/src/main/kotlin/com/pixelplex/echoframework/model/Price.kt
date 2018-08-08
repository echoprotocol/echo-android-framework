package com.pixelplex.echoframework.model

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import java.lang.reflect.Type

/**
 * The price struct stores asset prices in the Graphene system.
 *
 * A price is defined as a ratio between two assets, and represents a possible exchange rate
 * between those two assets. prices are generally not stored in any simplified form, i.e. a price
 * of (1000 CORE)/(20 USD) is perfectly normal.
 *
 * The assets within a price are labeled base and quote. Throughout the Graphene code base,
 * the convention used is that the base asset is the asset being sold, and the quote asset is
 * the asset being purchased, where the price is represented as base/quote, so in the example
 * price above the seller is looking to sell CORE asset and get USD in return.
 *
 * Note: Taken from the Graphene doxygen.
 *
 * @author Dmitriy Bushuev
 */
class Price : JsonSerializable, ByteSerializable {

    var base: AssetAmount? = null
    var quote: AssetAmount? = null

    override fun toBytes(): ByteArray {
        return base!!.toBytes() + quote!!.toBytes()
    }

    override fun toJsonString(): String? = null

    override fun toJsonObject(): JsonElement? = JsonObject().apply {
        add("base", base?.toJsonObject())
        add("quote", quote?.toJsonObject())
    }

    /**
     * Json deserializer for [Price] model
     */
    class PriceDeserializer : JsonDeserializer<Price> {

        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): Price? {
            if (json == null || !json.isJsonObject) return null

            val jsonPrice = json.asJsonObject

            val basePrice =
                context?.deserialize<AssetAmount>(jsonPrice.get("base"), AssetAmount::class.java)
            val quotePrice =
                context?.deserialize<AssetAmount>(jsonPrice.get("quote"), AssetAmount::class.java)

            return Price().apply {
                this.base = basePrice
                this.quote = quotePrice
            }
        }

    }

}
