package org.echo.mobile.framework.model

import com.google.gson.*
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import org.echo.mobile.framework.core.mapper.ObjectMapper
import java.lang.reflect.Type
import java.math.BigInteger
import java.util.*

/**
 * Represents a frozen balance model in Graphene blockchain
 *
 * @author Pavel Savchkov
 */
class FrozenBalanceObject(id: String,
        @Expose var owner: String,
        @SerializedName(ASSET_TYPE_KEY) @Expose var asset: Asset?,
        @Expose var multiplier: Int,
        @SerializedName(UNFREEZE_TIME_KEY) @Expose var unfreezeTime: Date?,
        @Expose var balance: BigInteger):GrapheneObject(id) {

    /**
     * Deserializer used to build a [FrozenBalanceObject] instance from JSON
     */
    class FrozenBalanceDeserializer : JsonDeserializer<FrozenBalanceObject> {

        override fun deserialize(
                json: JsonElement?,
                typeOfT: Type?,
                context: JsonDeserializationContext?
        ): FrozenBalanceObject? {

            if (json == null || !json.isJsonObject) return null

            val balanceObject = json.asJsonObject

            println(balanceObject.toString())

            val id = balanceObject[ID_KEY].asString
            val owner = balanceObject[OWNER_KEY].asString
            val asset = parseAsset(balanceObject)
            val multiplier = balanceObject[MULTIPLIER_KEY].asInt
            val unfreezeTime = Date(balanceObject[UNFREEZE_TIME_KEY].asLong)
            val balance = balanceObject[BALANCE_KEY].asLong.toBigInteger()

            return FrozenBalanceObject(id, owner, asset, multiplier,
                    unfreezeTime,
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
        private const val MULTIPLIER_KEY = "multiplier"
        private const val ASSET_TYPE_KEY = "asset_type"
        private const val UNFREEZE_TIME_KEY = "unfreeze_time"
        private const val BALANCE_KEY = "balance"
    }

    /**
     * Json mapper for [FrozenBalanceObject] model
     */
    class FrozenBalanceObjectMapper : ObjectMapper<FrozenBalanceObject> {

        override fun map(data: String): FrozenBalanceObject? =
                data.tryMapWithdraw()

        private fun String.tryMapWithdraw() =
                this.tryMap(FrozenBalanceObject::class.java)

        private fun <T> String.tryMap(resultType: Class<T>): T? =
                try {
                    GsonBuilder().registerTypeAdapter(
                            FrozenBalanceObject::class.java,
                            FrozenBalanceObject.FrozenBalanceDeserializer()
                    )
                            .create()
                            .fromJson(this, resultType)
                } catch (exception: Exception) {
                    null
                }
    }
}
