package org.echo.mobile.framework.model

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

/**
 * Represents full information about user account
 *
 * @author Daria Pechkovskaya
 */
class FullAccount(
    @Expose var account: Account? = null,
    @SerializedName("registrar_name") @Expose var registrarName: String? = null,
    @Expose var balances: List<Balance>? = null,
    @Expose var assets: List<Asset>? = null
) {

    /**
     * Deserializer used to build a [FullAccount] instance from JSON
     */
    class FullAccountDeserializer : JsonDeserializer<FullAccount> {

        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): FullAccount? {

            if (json == null || !json.isJsonObject) return null

            val fullAccountObject = json.asJsonObject

            val account = parseAccount(fullAccountObject, context)
            val registrar = fullAccountObject.get(REGISTRAR_KEY).asString
            val balances = parseBalances(fullAccountObject, context)
            val assets = parseAssets(fullAccountObject)

            return FullAccount(
                account,
                registrar,
                balances,
                assets
            )
        }

        private fun parseAccount(
            operationObject: JsonObject,
            deserializer: JsonDeserializationContext?
        ) = deserializer?.deserialize<Account>(
            operationObject.get(ACCOUNT_KEY),
            Account::class.java
        )

        private fun parseBalances(
            operationObject: JsonObject,
            deserializer: JsonDeserializationContext?
        ) = deserializer?.deserialize<List<Balance>>(
            operationObject.get(BALANCES_KEY),
            object : TypeToken<List<Balance>>() {}.type
        )

        private fun parseAssets(
            operationObject: JsonObject
        ): List<Asset>? {
            val assetsJson = operationObject[ASSETS_KEY].asJsonArray
            return assetsJson.map { element -> Asset(element.asString) }
        }

    }

    companion object {
        const val ACCOUNT_KEY = "account"
        const val REGISTRAR_KEY = "registrar_name"
        const val BALANCES_KEY = "balances"
        const val ASSETS_KEY = "assets"
        const val KEY_EXTENSIONS = "extensions"
        const val DEFAULT_ASSET_ID = "0"
    }

}
