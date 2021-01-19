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
 * Encapsulates freeze balance operation information
 *
 * @author Pavel Savchkov
 */
class BalanceFreezeOperation @JvmOverloads constructor(
        var account: Account,
        var amount: AssetAmount,
        var duration: Int,
        override var fee: AssetAmount = AssetAmount(UnsignedLong.ZERO)
) : BaseOperation(OperationType.BALANCE_FREEZE_OPERATION) {

    override fun toBytes(): ByteArray {
        val feeBytes = fee.toBytes()
        val accountBytes = account.toBytes()
        val amountBytes = amount.toBytes()
        val durationBytes = duration.toString().toByteArray()
        val extensions = extensions.toBytes()
        return Bytes.concat(
                feeBytes,
                accountBytes,
                amountBytes,
                durationBytes,
                extensions
        )
    }

    override fun toJsonString(): String {
        val gsonBuilder = GsonBuilder()
        gsonBuilder.registerTypeAdapter(
                BalanceFreezeOperation::class.java,
                BalanceFreezeSerializer()
        )
        return gsonBuilder.create().toJson(this)
    }

    override fun toJsonObject(): JsonElement = JsonArray().apply {
        add(id)
        val jsonObject = JsonObject().apply {
            add(KEY_FEE, fee.toJsonObject())
            addProperty(ACCOUNT_KEY, account.toJsonString())
            add(AMOUNT_KEY, amount.toJsonObject())
            addProperty(DURATION_KEY, duration)
            add(EXTENSIONS_KEY, extensions.toJsonObject())
        }
        add(jsonObject)
    }

    /**
     * Encapsulates logic of json serialization
     */
    class BalanceFreezeSerializer : JsonSerializer<BalanceFreezeOperation> {

        override fun serialize(
                balanceFreezeOperation: BalanceFreezeOperation,
                type: Type,
                jsonSerializationContext: JsonSerializationContext
        ): JsonElement = JsonArray().apply {
            add(balanceFreezeOperation.id)
            add(balanceFreezeOperation.toJsonObject())
        }
    }

    /**
     * This deserializer will work on balance freeze operation serialized
     * in the 'array form' used a lot in the Graphene Blockchain API.
     */
    class BalanceFreezeDeserializer : JsonDeserializer<BalanceFreezeOperation> {

        @Throws(JsonParseException::class)
        override fun deserialize(
                json: JsonElement?,
                typeOfT: Type,
                context: JsonDeserializationContext
        ): BalanceFreezeOperation? {

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
            val duration = jsonObject.get(DURATION_KEY).asInt

            return BalanceFreezeOperation(
                    account, amount, duration, fee
            )
        }
    }

    companion object {
        const val ACCOUNT_KEY = "account"
        const val AMOUNT_KEY = "amount"
        const val DURATION_KEY = "duration"
        const val EXTENSIONS_KEY = "extensions"
    }
}
