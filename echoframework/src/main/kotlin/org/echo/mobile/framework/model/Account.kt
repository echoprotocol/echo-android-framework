package org.echo.mobile.framework.model

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import org.echo.mobile.framework.model.eddsa.EdAuthority
import java.lang.reflect.Type

/**
 * Represents account model in blockchain
 *
 * @author Dmitriy Bushuev
 */
class Account : GrapheneObject, GrapheneSerializable {

    @Expose
    lateinit var name: String

    @Expose
    lateinit var active: EdAuthority

    @Expose
    lateinit var edKey: String

    @Expose
    lateinit var options: AccountOptions

    @Expose
    lateinit var statistics: String

    @Expose
    lateinit var registrar: String

    @Expose
    var networkFeePercentage: Long = 0

    @Expose
    @SerializedName("accumulated_reward")
    var accumulatedReward: Long = 0

    /**
     * Requires a user account in the string representation, that is in the 1.2.x format.
     */
    constructor(id: String) : super(id)

    /**
     * Constructor that requires the proper graphene object id and an account name,
     * that represent user account
     */
    constructor(id: String, name: String) : super(id) {
        this.name = name
    }

    override fun toJsonString(): String? = getObjectId()

    override fun toJsonObject(): JsonElement? = null

    override fun toString(): String = toJsonString() ?: ""

    /**
     * Deserializer used to build a [Account] instance from the JSON
     */
    class Deserializer : JsonDeserializer<Account> {

        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): Account? {
            if (json == null || !json.isJsonObject) return null

            val jsonAccount = json.asJsonObject

            return createAccountFromJson(jsonAccount).apply {
                registrar = jsonAccount.get(KEY_REGISTRAR).asString
                networkFeePercentage = jsonAccount.get(KEY_NETWORK_FEE_PERCENTAGE).asLong
                active = getAuthority(context!!, jsonAccount, KEY_ACTIVE)
                edKey = jsonAccount.get(KEY_ECHORAND_KEY).asString
                options = getOptions(context, jsonAccount)
                statistics = jsonAccount.get(KEY_STATISTICS).asString
            }
        }

        private fun createAccountFromJson(jsonAccount: JsonObject): Account {
            val id = jsonAccount.get(KEY_ID).asString
            val name = jsonAccount.get(KEY_NAME).asString
            return Account(id, name)
        }

        private fun getOptions(
            context: JsonDeserializationContext,
            jsonAccount: JsonObject
        ): AccountOptions =
            context.deserialize<AccountOptions>(
                jsonAccount.get(KEY_OPTIONS),
                AccountOptions::class.java
            )

        private fun getAuthority(
            context: JsonDeserializationContext,
            jsonAccount: JsonObject,
            key: String
        ): EdAuthority =
            context.deserialize<EdAuthority>(jsonAccount.get(key), EdAuthority::class.java)

    }

    companion object {
        const val PROXY_TO_SELF = "1.2.5"

        const val KEY_REGISTRAR = "registrar"
        const val KEY_NETWORK_FEE_PERCENTAGE = "network_fee_percentage"
        const val KEY_NAME = "name"
        const val KEY_ACTIVE = "active"
        const val KEY_ECHORAND_KEY = "echorand_key"
        const val KEY_OPTIONS = "options"
        const val KEY_STATISTICS = "statistics"
    }

}

/**
 * Check account equals by [key] from role [authorityType]
 *
 * @param key Public key from role
 * @param authorityType Role for equals operation
 */
fun Account.isEqualsByKey(key: String): Boolean = isKeyExist(key, active)

private fun isKeyExist(address: String, authority: EdAuthority): Boolean {
    val foundKey = authority.keyAuthorities.keys.find { pubKey ->
        pubKey.address == address
    }
    return foundKey != null
}

