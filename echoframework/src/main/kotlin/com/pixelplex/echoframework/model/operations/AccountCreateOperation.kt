package com.pixelplex.echoframework.model.operations

import com.google.common.primitives.Bytes
import com.google.common.primitives.UnsignedLong
import com.google.gson.*
import com.pixelplex.echoframework.model.*
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
    val registrar: String,
    val referrer: String,
    val referrerPercent: Int = 0,
    owner: Authority,
    active: Authority,
    options: AccountOptions,
    override var fee: AssetAmount = AssetAmount(UnsignedLong.valueOf(0), Asset("1.3.0"))
) : BaseOperation(OperationType.ACCOUNT_UPDATE_OPERATION) {

    private var owner = Optional(owner)
    private var active = Optional(active)
    private var options = Optional(options)

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
            addProperty(KEY_REGISTRAR, registrar)
            addProperty(KEY_REFERRER, referrer)
            addProperty(KEY_REFERRER_PERCENT, referrerPercent)

            if (owner.isSet)
                add(KEY_OWNER, owner.toJsonObject())
            if (active.isSet)
                add(KEY_ACTIVE, active.toJsonObject())
            if (options.isSet)
                add(KEY_OPTIONS, options.toJsonObject())

            add(KEY_EXTENSIONS, extensions.toJsonObject())
        }

        array.add(accountUpdate)
        return array
    }

    override fun toBytes(): ByteArray {
        val feeBytes = fee.toBytes()
        val nameBytes = name.toByteArray()
        val registrar = registrar.toByteArray()
        val referrer = referrer.toByteArray()
        val referrerPercent = byteArrayOf(referrerPercent.toByte())
        val ownerBytes = owner.toBytes()
        val activeBytes = active.toBytes()
        val newOptionsBytes = options.toBytes()
        val extensionBytes = extensions.toBytes()
        return Bytes.concat(
            feeBytes,
            registrar,
            referrer,
            referrerPercent,
            nameBytes,
            ownerBytes,
            activeBytes,
            newOptionsBytes,
            extensionBytes
        )
    }

    /**
     * Deserializer used to build a [AccountCreateOperation] instance from JSON
     */
    class AccountCreateDeserializer : JsonDeserializer<AccountCreateOperation> {

        @Throws(JsonParseException::class)
        override fun deserialize(
            json: JsonElement,
            typeOfT: Type,
            context: JsonDeserializationContext
        ): AccountCreateOperation? {
            val jsonObject = json.asJsonObject

            val name = jsonObject.get(KEY_NAME).asString
            val referrer = jsonObject.get(KEY_REFERRER).asString
            val registrar = jsonObject.get(KEY_REGISTRAR).asString

            // Deserializing Authority objects
            val owner =
                context.deserialize<Authority>(jsonObject.get(KEY_OWNER), Authority::class.java)
            val active =
                context.deserialize<Authority>(jsonObject.get(KEY_ACTIVE), Authority::class.java)

            //Deserializing AccountOptions object
            val options = context.deserialize<AccountOptions>(
                jsonObject.get(KEY_OPTIONS),
                AccountOptions::class.java
            )

            // Deserializing AssetAmount object
            val fee =
                context.deserialize<AssetAmount>(jsonObject.get(KEY_FEE), AssetAmount::class.java)

            return AccountCreateOperation(name, registrar, referrer, 0, owner, active, options, fee)
        }

    }

    companion object {
        const val KEY_NAME = "name"
        const val KEY_REGISTRAR = "registrar"
        const val KEY_REFERRER = "referrer"
        const val KEY_REFERRER_PERCENT = "referrer_percent"
        const val KEY_OWNER = "owner"
        const val KEY_ACTIVE = "active"
        const val KEY_FEE = "fee"
        const val KEY_OPTIONS = "options"
        const val KEY_EXTENSIONS = "extensions"
    }

}
