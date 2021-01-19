package org.echo.mobile.framework.model.operations

import com.google.common.primitives.Bytes
import com.google.common.primitives.UnsignedLong
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import org.echo.mobile.framework.model.Account
import org.echo.mobile.framework.model.AssetAmount
import org.echo.mobile.framework.model.BaseOperation
import java.lang.reflect.Type

/**
 * Encapsulates unfreeze balance operation information
 *
 * @author Pavel Savchkov
 */
class BalanceUnfreezeOperation @JvmOverloads constructor(
        var account: Account,
        var amount: AssetAmount,
        override var fee: AssetAmount = AssetAmount(UnsignedLong.ZERO)
) : BaseOperation(OperationType.BALANCE_UNFREEZE_OPERATION) {

    override fun toBytes(): ByteArray {
        val feeBytes = fee.toBytes()
        val accountBytes = account.toBytes()
        val amountBytes = amount.toBytes()
        val extensions = extensions.toBytes()
        return Bytes.concat(
                feeBytes,
                accountBytes,
                amountBytes,
                extensions
        )
    }

    override fun toJsonString(): String {
        val gsonBuilder = GsonBuilder()
        gsonBuilder.registerTypeAdapter(
                BalanceUnfreezeOperation::class.java,
                BalanceUnfreezeSerializer()
        )
        return gsonBuilder.create().toJson(this)
    }

    override fun toJsonObject(): JsonElement = JsonArray().apply {
        add(id)
        val jsonObject = JsonObject().apply {
            add(KEY_FEE, fee.toJsonObject())
            addProperty(ACCOUNT_KEY, account.toJsonString())
            add(AMOUNT_KEY, amount.toJsonObject())
            add(EXTENSIONS_KEY, extensions.toJsonObject())
        }
        add(jsonObject)
    }

    /**
     * Encapsulates logic of json serialization
     */
    class BalanceUnfreezeSerializer : JsonSerializer<BalanceUnfreezeOperation> {

        override fun serialize(
                balanceUnfreezeOperation: BalanceUnfreezeOperation,
                type: Type,
                jsonSerializationContext: JsonSerializationContext
        ): JsonElement = JsonArray().apply {
            add(balanceUnfreezeOperation.id)
            add(balanceUnfreezeOperation.toJsonObject())
        }
    }

    /**
     * This deserializer will work on balance unfreeze operation serialized
     * in the 'array form' used a lot in the Graphene Blockchain API.
     */
    class BalanceUnfreezeDeserializer : JsonDeserializer<BalanceUnfreezeOperation> {

        @Throws(JsonParseException::class)
        override fun deserialize(
                json: JsonElement?,
                typeOfT: Type,
                context: JsonDeserializationContext
        ): BalanceUnfreezeOperation? {

            if (json == null || !json.isJsonObject) return null

            val jsonObject = json.asJsonObject

            val fee = context.deserialize<AssetAmount>(
                    jsonObject.get(KEY_FEE),
                    AssetAmount::class.java
            )

            val account = Account(jsonObject.get(ACCOUNT_KEY).asString)
            val amount =
                    context.deserialize<AssetAmount>(jsonObject.get(AMOUNT_KEY),
                            AssetAmount::class.java)

            return BalanceUnfreezeOperation(
                    account, amount, fee
            )
        }
    }

    companion object {
        const val ACCOUNT_KEY = "account"
        const val AMOUNT_KEY = "amount"
        const val EXTENSIONS_KEY = "extensions"
    }
}
