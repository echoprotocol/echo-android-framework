package com.pixelplex.echoframework.model

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type
import java.math.BigInteger

/**
 * Represents balance model in Graphene blockchain
 *
 * @author Dmitriy Bushuev
 */
data class Balance(
    @Expose var id: String,
    @Expose var owner: String,
    @SerializedName(ASSET_TYPE_KEY) @Expose var asset: Asset?,
    @Expose var balance: BigInteger
) {

    /**
     * Deserializer used to build a [Balance] instance from JSON
     */
    class BalanceDeserializer : JsonDeserializer<Balance> {

        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): Balance? {

            if (json == null || !json.isJsonObject) return null

            val balanceObject = json.asJsonObject

            val id = balanceObject[ID_KEY].asString
            val owner = balanceObject[OWNER_KEY].asString
            val asset = parseAsset(balanceObject)
            val balance = balanceObject[BALANCE_KEY].asLong.toBigInteger()

            return Balance(id, owner, asset, balance)
        }

        private fun parseAsset(
            operationObject: JsonObject
        ): Asset? {
            val assetsJson = operationObject[ASSET_TYPE_KEY].asString
            return Asset(assetsJson)
        }

    }

    companion object {
        private const val ID_KEY = "id"
        private const val OWNER_KEY = "owner"
        private const val ASSET_TYPE_KEY = "asset_type"
        private const val BALANCE_KEY = "balance"
    }

}
