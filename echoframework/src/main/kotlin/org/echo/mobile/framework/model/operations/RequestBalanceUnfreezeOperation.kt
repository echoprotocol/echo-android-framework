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
import org.echo.mobile.framework.support.toJsonObject
import java.lang.reflect.Type

/**
 * Encapsulates unfreeze balance operation information
 *
 * @author Pavel Savchkov
 */
class RequestBalanceUnfreezeOperation @JvmOverloads constructor(
        override var fee: AssetAmount = AssetAmount(UnsignedLong.ZERO),
        var account: Account,
        var objectsToUnfreeze: List<String>
) : BaseOperation(OperationType.REQUEST_BALANCE_UNFREEZE_OPERATION) {

    override fun toBytes(): ByteArray {
        val feeBytes = fee.toBytes()
        val accountBytes = account.toBytes()
        val objectsToUnfreezeBytes = objectsToUnfreeze.joinToString().toByteArray()
        val extensions = extensions.toBytes()
        return Bytes.concat(
                feeBytes,
                accountBytes,
                objectsToUnfreezeBytes,
                extensions
        )
    }

    override fun toJsonString(): String {
        val gsonBuilder = GsonBuilder()
        gsonBuilder.registerTypeAdapter(
                RequestBalanceUnfreezeOperation::class.java,
                RequestBalanceUnfreezeSerializer()
        )
        return gsonBuilder.create().toJson(this)
    }

    override fun toJsonObject(): JsonElement = JsonArray().apply {
        add(id)
        val jsonObject = JsonObject().apply {
            add(KEY_FEE, fee.toJsonObject())
            add(ACCOUNT_KEY, account.toJsonObject())
            add(OBJECTS_TO_UNFREEZE_KEY, objectsToUnfreeze.joinToString().toJsonObject())
            add(EXTENSIONS_KEY, extensions.toJsonObject())
        }
        add(jsonObject)
    }

    /**
     * Encapsulates logic of json serialization
     */
    class RequestBalanceUnfreezeSerializer : JsonSerializer<RequestBalanceUnfreezeOperation> {

        override fun serialize(
                requestBalanceUnfreezeOperation: RequestBalanceUnfreezeOperation,
                type: Type,
                jsonSerializationContext: JsonSerializationContext
        ): JsonElement = JsonArray().apply {
            add(requestBalanceUnfreezeOperation.id)
            add(requestBalanceUnfreezeOperation.toJsonObject())
        }
    }

    /**
     * This deserializer will work on request balance unfreeze operation serialized
     * in the 'array form' used a lot in the Graphene Blockchain API.
     */
    class RequestBalanceUnfreezeDeserializer : JsonDeserializer<RequestBalanceUnfreezeOperation> {

        @Throws(JsonParseException::class)
        override fun deserialize(
                json: JsonElement?,
                typeOfT: Type,
                context: JsonDeserializationContext
        ): RequestBalanceUnfreezeOperation? {

            if (json == null || !json.isJsonObject) return null

            val jsonObject = json.asJsonObject

            val fee = context.deserialize<AssetAmount>(
                    jsonObject.get(KEY_FEE),
                    AssetAmount::class.java
            )

            val account = Account(jsonObject.get(ACCOUNT_KEY).asString)
            val objectsToUnfreeze = jsonObject.get(OBJECTS_TO_UNFREEZE_KEY).toString().split(",")

            return RequestBalanceUnfreezeOperation(
                    fee, account, objectsToUnfreeze
            )
        }
    }

    companion object {
        const val ACCOUNT_KEY = "account"
        const val OBJECTS_TO_UNFREEZE_KEY = "objects_to_unfreeze"
        const val EXTENSIONS_KEY = "extensions"
    }
}
