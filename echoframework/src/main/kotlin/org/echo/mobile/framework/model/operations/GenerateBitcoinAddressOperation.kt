package org.echo.mobile.framework.model.operations

import com.google.common.primitives.Bytes
import com.google.common.primitives.UnsignedLong
import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import org.echo.mobile.framework.model.Account
import org.echo.mobile.framework.model.AssetAmount
import org.echo.mobile.framework.model.BaseOperation
import org.echo.mobile.framework.support.Uint8
import java.lang.reflect.Type

/**
 * Generates address for botcoin network transfers for required target account [account]
 *
 * @param account Target account for bitcoin address generation
 *
 * @author Dmitriy Bushuev
 */
class GenerateBitcoinAddressOperation constructor(
    var account: Account,
    val backupAddress: String,
    override var fee: AssetAmount = AssetAmount(UnsignedLong.ZERO)
) : BaseOperation(OperationType.SIDECHAIN_BTC_CREATE_ADDRESS_OPERATION) {

    override fun toBytes(): ByteArray {
        val feeBytes = fee.toBytes()
        val accountIdBytes = account.toBytes()
        val backupBytes = Uint8.serialize(backupAddress.length) + backupAddress.toByteArray()

        return Bytes.concat(feeBytes, accountIdBytes, backupBytes)
    }

    override fun toJsonString(): String? = toJsonObject().toString()

    override fun toJsonObject(): JsonElement =
        JsonArray().apply {
            add(id)

            val accountUpdate = JsonObject().apply {
                add(KEY_FEE, fee.toJsonObject())
                addProperty(ACCOUNT_ID_KEY, account.toJsonString())
                addProperty(BACKUP_ADDRESS, backupAddress)
                add(AccountUpdateOperation.KEY_EXTENSIONS, extensions.toJsonObject())
            }

            add(accountUpdate)
        }

    override fun toString(): String = "${javaClass.simpleName}(${toJsonObject()})"

    /**
     * JSON deserializer for [GenerateBitcoinAddressOperation]
     */
    class GenerateBitcoinAddressDeserializer : JsonDeserializer<GenerateBitcoinAddressOperation> {

        @Throws(JsonParseException::class)
        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type,
            context: JsonDeserializationContext
        ): GenerateBitcoinAddressOperation? {
            if (json == null || !json.isJsonObject) return null

            val jsonObject = json.asJsonObject

            val fee = context.deserialize<AssetAmount>(
                jsonObject.get(KEY_FEE),
                AssetAmount::class.java
            )

            val backupAddress = jsonObject.get(BACKUP_ADDRESS).asString

            val account = Account(
                jsonObject.get(ACCOUNT_ID_KEY).asString
            )

            return GenerateBitcoinAddressOperation(
                account,
                backupAddress,
                fee
            )
        }
    }

    companion object {
        private const val ACCOUNT_ID_KEY = "account"
        private const val BACKUP_ADDRESS = "backup_address"
    }

}
