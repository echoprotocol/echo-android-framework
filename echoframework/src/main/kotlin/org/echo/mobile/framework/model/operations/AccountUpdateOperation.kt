package org.echo.mobile.framework.model.operations

import com.google.common.primitives.Bytes
import com.google.common.primitives.UnsignedLong
import com.google.gson.*
import org.echo.mobile.framework.model.*
import java.lang.reflect.Type

/**
 * Represents blockchain operation for updating an existing account.
 *
 * @author Daria Pechkovskaya
 * @author Dmitriy Bushuev
 */
class AccountUpdateOperation @JvmOverloads constructor(
    var account: Account,
    owner: Authority?,
    active: Authority?,
    newOptions: AccountOptions?,
    override var fee: AssetAmount = AssetAmount(UnsignedLong.ZERO)
) : BaseOperation(OperationType.ACCOUNT_UPDATE_OPERATION) {

    var ownerOption = Optional(owner)
    var activeOption = Optional(active)
    var newOptionsOption = Optional(newOptions, true)

    /**
     * Updates owner value
     * @param owner New owner value
     */
    fun setOwner(owner: Authority) {
        this.ownerOption = Optional(owner)
    }

    /**
     * Updates active value
     * @param active New active value
     */
    fun setActive(active: Authority) {
        this.activeOption = Optional(active)
    }

    /**
     * Updates options value
     * @param options New options value
     */
    fun setAccountOptions(options: AccountOptions) {
        this.newOptionsOption = Optional(options)
    }

    override fun toBytes(): ByteArray {
        val feeBytes = fee.toBytes()
        val accountBytes = account.toBytes()
        val ownerBytes = ownerOption.toBytes()
        val activeBytes = activeOption.toBytes()
        val newOptionsBytes = newOptionsOption.toBytes()
        val extensionBytes = extensions.toBytes()
        return Bytes.concat(
            feeBytes,
            accountBytes,
            ownerBytes,
            activeBytes,
            newOptionsBytes,
            extensionBytes
        )
    }

    override fun toJsonString(): String? = Gson().toJson(this)

    override fun toJsonObject(): JsonElement =
        JsonArray().apply {
            add(id)

            val accountUpdate = JsonObject().apply {
                add(KEY_FEE, fee.toJsonObject())
                addProperty(KEY_ACCOUNT, account.toJsonString())
                if (ownerOption.isSet)
                    add(KEY_OWNER, ownerOption.toJsonObject())
                if (activeOption.isSet)
                    add(KEY_ACTIVE, activeOption.toJsonObject())
                if (newOptionsOption.isSet)
                    add(KEY_NEW_OPTIONS, newOptionsOption.toJsonObject())
                add(KEY_EXTENSIONS, extensions.toJsonObject())
            }

            add(accountUpdate)
        }

    override fun toString(): String = "${javaClass.simpleName}(${toJsonObject()})"

    /**
     * Deserializer used to build a [AccountUpdateOperation] instance from JSON
     */
    class Deserializer : JsonDeserializer<AccountUpdateOperation> {

        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext?
        ): AccountUpdateOperation? {

            if (json == null || !json.isJsonObject) return null

            val operationObject = json.asJsonObject

            val account = createAccount(operationObject)
            val owner = parseOwner(operationObject, context)
            val active = parseActive(operationObject, context)
            val newOptions = parseNewOptions(operationObject, context)
            val fee = parseFee(operationObject, context)

            return AccountUpdateOperation(
                account,
                owner,
                active,
                newOptions,
                fee ?: AssetAmount(UnsignedLong.ZERO)
            )
        }

        private fun createAccount(operationObject: JsonObject) =
            Account(operationObject.get(KEY_ACCOUNT).asString)

        private fun parseOwner(
            operationObject: JsonObject,
            deserializer: JsonDeserializationContext?
        ) = deserializer?.deserialize<Authority>(
            operationObject.get(KEY_OWNER),
            Authority::class.java
        )

        private fun parseActive(
            operationObject: JsonObject,
            deserializer: JsonDeserializationContext?
        ) = deserializer?.deserialize<Authority>(
            operationObject.get(KEY_ACTIVE),
            Authority::class.java
        )

        private fun parseNewOptions(
            operationObject: JsonObject,
            deserializer: JsonDeserializationContext?
        ) = deserializer?.deserialize<AccountOptions>(
            operationObject.get(KEY_NEW_OPTIONS),
            AccountOptions::class.java
        )

        private fun parseFee(
            operationObject: JsonObject,
            deserializer: JsonDeserializationContext?
        ) = deserializer?.deserialize<AssetAmount>(
            operationObject.get(KEY_FEE),
            AssetAmount::class.java
        )

    }

    companion object {
        const val KEY_ACCOUNT = "account"
        const val KEY_OWNER = "owner"
        const val KEY_ACTIVE = "active"
        const val KEY_NEW_OPTIONS = "new_options"
        const val KEY_EXTENSIONS = "extensions"
    }

}
