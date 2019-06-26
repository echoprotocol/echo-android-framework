package org.echo.mobile.framework.model.operations

import com.google.common.primitives.Bytes
import com.google.common.primitives.UnsignedLong
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import org.echo.mobile.framework.model.Account
import org.echo.mobile.framework.model.AccountOptions
import org.echo.mobile.framework.model.AssetAmount
import org.echo.mobile.framework.model.BaseOperation
import org.echo.mobile.framework.model.Optional
import org.echo.mobile.framework.model.eddsa.EdAuthority
import java.lang.reflect.Type

/**
 * Class used to encapsulate operations related to the [OperationType.ACCOUNT_CREATE_OPERATION]
 *
 * @author Dmitriy Bushuev
 */
class AccountCreateOperation
/**
 * Account create operation constructor.
 *
 * @param name      User name. Can't be null.
 * @param registrar User id. Can't be null.
 * @param referrer  User id. Can't be null.
 * @param owner     Owner authority to set. Can be null.
 * @param active    Active authority to set. Can be null.
 * @param options   Active authority to set. Can be null.
 * @param fee       The fee to pay. Can be null.
 */
@JvmOverloads constructor(
    val name: String,
    var registrar: Account,
    var referrer: Account,
    val referrerPercent: Int = 0,
    active: EdAuthority,
    private val edKey: String,
    options: AccountOptions,
    override var fee: AssetAmount = AssetAmount(UnsignedLong.ZERO)
) : BaseOperation(OperationType.ACCOUNT_CREATE_OPERATION) {

    private var active = Optional(active)
    private var options = Optional(options)

    /**
     * Updates active value
     * @param active New active value
     */
    fun setActive(active: EdAuthority) {
        this.active = Optional(active)
    }

    /**
     * Updates options value
     * @param options New options value
     */
    fun setAccountOptions(options: AccountOptions) {
        this.options = Optional(options)
    }

    override fun toJsonString(): String {
        val gson = Gson()
        return gson.toJson(this)
    }

    override fun toJsonObject(): JsonElement {
        val array = JsonArray()
        array.add(this.id)

        val accountUpdate = JsonObject().apply {
            add(KEY_FEE, fee.toJsonObject())
            addProperty(KEY_NAME, name)
            addProperty(KEY_REGISTRAR, registrar.toJsonString())
            addProperty(KEY_REFERRER, referrer.toJsonString())
            addProperty(KEY_REFERRER_PERCENT, referrerPercent)

            if (active.isSet) add(KEY_ACTIVE, active.toJsonObject())

            addProperty(KEY_ACTIVE, edKey)

            if (options.isSet) add(KEY_OPTIONS, options.toJsonObject())

            add(KEY_EXTENSIONS, extensions.toJsonObject())
        }

        array.add(accountUpdate)
        return array
    }

    override fun toBytes(): ByteArray {
        val feeBytes = fee.toBytes()
        val nameBytes = name.toByteArray()
        val registrar = registrar.toBytes()
        val referrer = referrer.toBytes()
        val referrerPercent = byteArrayOf(referrerPercent.toByte())
        val activeBytes = active.toBytes()
        val edKeyBytes = edKey.toByteArray()
        val newOptionsBytes = options.toBytes()
        val extensionBytes = extensions.toBytes()
        return Bytes.concat(
            feeBytes,
            registrar,
            referrer,
            referrerPercent,
            nameBytes,
            activeBytes,
            edKeyBytes,
            newOptionsBytes,
            extensionBytes
        )
    }

    /**
     * Deserializer used to build a [AccountCreateOperation] instance from JSON
     */
    class Deserializer : JsonDeserializer<AccountCreateOperation> {

        @Throws(JsonParseException::class)
        override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext
        ): AccountCreateOperation? {
            val jsonObject = json.asJsonObject

            val name = jsonObject.get(KEY_NAME).asString
            val referrer = Account(jsonObject.get(KEY_REFERRER).asString)
            val registrar = Account(jsonObject.get(KEY_REGISTRAR).asString)

            // Deserializing EdAuthority objects
            val active =
                context.deserialize<EdAuthority>(
                    jsonObject.get(KEY_ACTIVE),
                    EdAuthority::class.java
                )
            val edKey = jsonObject.get(KEY_ED_KEY).asString

            //Deserializing AccountOptions object
            val options = context.deserialize<AccountOptions>(
                jsonObject.get(KEY_OPTIONS),
                AccountOptions::class.java
            )

            // Deserializing AssetAmount object
            val fee =
                context.deserialize<AssetAmount>(jsonObject.get(KEY_FEE), AssetAmount::class.java)

            return AccountCreateOperation(
                name,
                registrar,
                referrer,
                0,
                active,
                edKey,
                options,
                fee
            )
        }

    }

    companion object {
        const val KEY_NAME = "name"
        const val KEY_REGISTRAR = "registrar"
        const val KEY_REFERRER = "referrer"
        const val KEY_REFERRER_PERCENT = "referrer_percent"
        const val KEY_ACTIVE = "active"
        const val KEY_ED_KEY = "echorand_key"
        const val KEY_OPTIONS = "options"
        const val KEY_EXTENSIONS = "extensions"
    }

}
