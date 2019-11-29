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
import org.echo.mobile.framework.support.serialize
import java.lang.reflect.Type

/**
 * Virtual operation that indicates payout of block reward
 *
 * @author Dmitriy Bushuev
 */
class BlockRewardOperation @JvmOverloads constructor(
    var reciever: Account,
    val amount: String,
    override var fee: AssetAmount = AssetAmount(UnsignedLong.ZERO)
) : BaseOperation(OperationType.BLOCK_REWARD_OPERATION) {

    override fun toJsonString(): String? {
        return toJsonObject().toString()
    }

    override fun toJsonObject(): JsonElement = JsonArray().apply {
        add(id)
        val jsonObject = JsonObject().apply {
            add(KEY_FEE, fee.toJsonObject())
            addProperty(KEY_RECEIVER, reciever.toJsonString())
            addProperty(KEY_AMOUNT, amount)
            add(TransferOperation.KEY_EXTENSIONS, extensions.toJsonObject())
        }
        add(jsonObject)
    }

    override fun toString(): String = "${javaClass.simpleName}(${toJsonObject()})"

    override fun toBytes(): ByteArray {
        val receiverBytes = reciever.toBytes()
        val amountBytes = amount.serialize()
        val feeBytes = fee.toBytes()
        val extensionBytes = extensions.toBytes()

        return Bytes.concat(feeBytes, receiverBytes, amountBytes, extensionBytes)
    }

    /**
     * Deserializer for [BlockRewardOperation] json string
     */
    class BlockRewardDeserializer : JsonDeserializer<BlockRewardOperation> {

        @Throws(JsonParseException::class)
        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type,
            context: JsonDeserializationContext
        ): BlockRewardOperation? {

            if (json == null || !json.isJsonObject) return null

            val jsonObject = json.asJsonObject

            val amount = jsonObject.get(KEY_AMOUNT).asString
            val fee = context.deserialize<AssetAmount>(
                jsonObject.get(KEY_FEE),
                AssetAmount::class.java
            ) ?: AssetAmount(UnsignedLong.ZERO)

            val receiver = Account(jsonObject.get(KEY_RECEIVER).asString)

            return BlockRewardOperation(
                receiver,
                amount,
                fee
            )
        }
    }

    companion object {
        const val KEY_RECEIVER = "reciever"
        const val KEY_AMOUNT = "amount"
    }

}
