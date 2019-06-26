package org.echo.mobile.framework.model.operations

import com.google.common.primitives.Bytes
import com.google.common.primitives.UnsignedLong
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.echo.mobile.framework.model.Account
import org.echo.mobile.framework.model.AccountOptions
import org.echo.mobile.framework.model.AssetAmount
import org.echo.mobile.framework.model.BaseOperation
import org.echo.mobile.framework.model.Optional
import org.echo.mobile.framework.model.eddsa.EdAddress
import org.echo.mobile.framework.model.eddsa.EdAuthority
import java.lang.reflect.Type

/**
 * Represents blockchain operation for updating an existing account.
 *
 * @author Daria Pechkovskaya
 * @author Dmitriy Bushuev
 */
class AccountUpdateOperation @JvmOverloads constructor(
    var account: Account,
    active: EdAuthority?,
    private val edKey: String?,
    newOptions: AccountOptions?,
    override var fee: AssetAmount = AssetAmount(UnsignedLong.ZERO)
) : BaseOperation(OperationType.ACCOUNT_UPDATE_OPERATION) {

    var activeOption = Optional(active)
    var newOptionsOption = Optional(newOptions, true)

    /**
     * Updates active value
     * @param active New active value
     */
    fun setActive(active: EdAuthority) {
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
        val activeBytes = activeOption.toBytes()

        val edKeyBytes = edKey?.let {
            byteArrayOf(1) + EdAddress(edKey).pubKey.toBytes()
        } ?: byteArrayOf(0)

        val newOptionsBytes = newOptionsOption.toBytes()
        val extensionBytes = extensions.toBytes()
        return Bytes.concat(
            feeBytes,
            accountBytes,
            activeBytes,
            edKeyBytes,
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
                if (activeOption.isSet) add(KEY_ACTIVE, activeOption.toJsonObject())
                if (edKey != null) addProperty(KEY_ED_KEY, edKey)
                if (newOptionsOption.isSet) add(KEY_NEW_OPTIONS, newOptionsOption.toJsonObject())
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
            val active = parseActive(operationObject, context)
            val edKey = operationObject.get(AccountCreateOperation.KEY_ED_KEY).asString
            val newOptions = parseNewOptions(operationObject, context)
            val fee = parseFee(operationObject, context)

            return AccountUpdateOperation(
                account,
                active,
                edKey,
                newOptions,
                fee ?: AssetAmount(UnsignedLong.ZERO)
            )
        }

        private fun createAccount(operationObject: JsonObject) =
            Account(operationObject.get(KEY_ACCOUNT).asString)

        private fun parseActive(
            operationObject: JsonObject,
            deserializer: JsonDeserializationContext?
        ) = deserializer?.deserialize<EdAuthority>(
            operationObject.get(KEY_ACTIVE),
            EdAuthority::class.java
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
        const val KEY_ACTIVE = "active"
        const val KEY_ED_KEY = "echorand_key"
        const val KEY_NEW_OPTIONS = "new_options"
        const val KEY_EXTENSIONS = "extensions"
    }

}
