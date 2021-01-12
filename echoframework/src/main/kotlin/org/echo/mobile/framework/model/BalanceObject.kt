package org.echo.mobile.framework.model

import com.google.gson.*
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import org.echo.mobile.framework.core.mapper.ObjectMapper
import java.lang.reflect.Type
import java.math.BigInteger
import java.util.*

/**
 * Represents balance model in Graphene blockchain
 *
 * @author Pavel Savchkov
 */
class BalanceObject(id: String,
                    @Expose var owner: String,
                    @SerializedName(ASSET_TYPE_KEY) @Expose var asset: Asset?,
                    @SerializedName(LAST_CLAIM_DATE_KEY) @Expose var lastClaimData: Date?,
                    @Expose var balance: BigInteger
) : GrapheneObject(id) {

    /**
     * Deserializer used to build a [AccountBalance] instance from JSON
     */
    class BalanceDeserializer : JsonDeserializer<BalanceObject> {

        override fun deserialize(
                json: JsonElement?,
                typeOfT: Type?,
                context: JsonDeserializationContext?
        ): BalanceObject? {

            if (json == null || !json.isJsonObject) return null

            val balanceObject = json.asJsonObject

            println(balanceObject.toString())

            val id = balanceObject[ID_KEY].asString
            val owner = balanceObject[OWNER_KEY].asString
            val asset = parseAsset(balanceObject)
            val lastClaimDate = Date(balanceObject[LAST_CLAIM_DATE_KEY].asLong)
            val balance = balanceObject[BALANCE_KEY].asLong.toBigInteger()

            return BalanceObject(id, owner, asset,
                    lastClaimDate,
                    balance)
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
        private const val LAST_CLAIM_DATE_KEY = "last_claim_date"
        private const val BALANCE_KEY = "balance"
    }

    /**
     * Json mapper for [BalanceObject] model
     */
    class BalanceObjectMapper : ObjectMapper<BalanceObject> {

        override fun map(data: String): BalanceObject? =
                data.tryMapWithdraw()

        private fun String.tryMapWithdraw() =
                this.tryMap(BalanceObject::class.java)

        private fun <T> String.tryMap(resultType: Class<T>): T? =
                try {
                    GsonBuilder().registerTypeAdapter(
                            BalanceObject::class.java,
                            BalanceDeserializer()
                    )
                            .create()
                            .fromJson(this, resultType)
                } catch (exception: Exception) {
                    null
                }
    }
}
