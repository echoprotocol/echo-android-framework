package org.echo.mobile.framework.model

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.annotations.Expose
import org.echo.mobile.framework.TIME_DATE_FORMAT
import java.lang.reflect.Type
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Represents account model in Graphene blockchain
 * [Account model documentation](https://dev-doc.myecho.app/classgraphene_1_1chain_1_1account__object.html)
 *
 * @author Dmitriy Bushuev
 */
class Account : GrapheneObject, GrapheneSerializable {

    @Expose
    lateinit var name: String

    @Expose
    lateinit var owner: Authority

    @Expose
    lateinit var active: Authority

    @Expose
    lateinit var options: AccountOptions

    @Expose
    lateinit var statistics: String

    @Expose
    var membershipExpirationDate: Long = 0

    @Expose
    lateinit var registrar: String

    @Expose
    lateinit var referrer: String

    @Expose
    lateinit var lifetimeReferrer: String

    @Expose
    var networkFeePercentage: Long = 0

    @Expose
    var lifetimeReferrerFeePercentage: Long = 0

    @Expose
    var referrerRewardsPercentage: Long = 0

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
                membershipExpirationDate = getDate(jsonAccount)
                referrer = jsonAccount.get(KEY_REFERRER).asString
                lifetimeReferrer = jsonAccount.get(KEY_LIFETIME_REFERRER).asString
                networkFeePercentage = jsonAccount.get(KEY_NETWORK_FEE_PERCENTAGE).asLong
                lifetimeReferrerFeePercentage =
                        jsonAccount.get(KEY_LIFETIME_REFERRER_FEE_PERCENTAGE).asLong
                referrerRewardsPercentage =
                        jsonAccount.get(KEY_REFERRER_REWARD_PERCENTAGE).asLong
                owner = getAuthority(context!!, jsonAccount, KEY_OWNER)
                active = getAuthority(context, jsonAccount, KEY_ACTIVE)
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
        ): Authority =
            context.deserialize<Authority>(jsonAccount.get(key), Authority::class.java)

        private fun getDate(jsonAccount: JsonObject): Long {
            val dateFormat = SimpleDateFormat(TIME_DATE_FORMAT, Locale.getDefault())
            return try {
                dateFormat.parse(jsonAccount.get(KEY_MEMBERSHIP_EXPIRATION_DATE).asString).time
            } catch (e: ParseException) {
                0
            }
        }

    }

    companion object {
        const val PROXY_TO_SELF = "1.2.5"

        const val KEY_MEMBERSHIP_EXPIRATION_DATE = "membership_expiration_date"
        const val KEY_REGISTRAR = "registrar"
        const val KEY_REFERRER = "referrer"
        const val KEY_LIFETIME_REFERRER = "lifetime_referrer"
        const val KEY_NETWORK_FEE_PERCENTAGE = "network_fee_percentage"
        const val KEY_LIFETIME_REFERRER_FEE_PERCENTAGE = "lifetime_referrer_fee_percentage"
        const val KEY_REFERRER_REWARD_PERCENTAGE = "referrer_rewards_percentage"
        const val KEY_NAME = "name"
        const val KEY_OWNER = "owner"
        const val KEY_ACTIVE = "active"
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
fun Account.isEqualsByKey(key: String, authorityType: AuthorityType): Boolean =
    when (authorityType) {
        AuthorityType.OWNER -> isKeyExist(key, owner)
        AuthorityType.ACTIVE -> isKeyExist(key, active)
        AuthorityType.KEY -> {
            options.memoKey?.address == key
        }
    }

private fun isKeyExist(address: String, authority: Authority): Boolean {
    val foundKey = authority.keyAuthorities.keys.find { pubKey ->
        pubKey.address == address
    }
    return foundKey != null
}

