package com.pixelplex.echoframework.model.operations

import com.google.common.primitives.Bytes
import com.google.common.primitives.UnsignedLong
import com.google.gson.*
import com.pixelplex.echoframework.model.Account
import com.pixelplex.echoframework.model.AssetAmount
import com.pixelplex.echoframework.model.BaseOperation
import com.pixelplex.echoframework.model.contract.Contract
import java.lang.reflect.Type

/**
 * Represents blockchain operation for transferring amount from contract.
 *
 * @author Daria Pechkovskaya
 */
class ContractTransferOperation
/**
 *  Contract transfer operation constructor.
 *
 *  @param fee          Fee to pay.
 *  @param from         Contract to transfer asset from.
 *  @param to           Account or contract to transfer asset to.
 *  @param amount       The amount of asset to transfer from from to to.
 */
@JvmOverloads
constructor(
    override var fee: AssetAmount = AssetAmount(UnsignedLong.ZERO),
    val from: Contract,
    val to: Account,
    val amount: AssetAmount

) : BaseOperation(OperationType.CONTRACT_TRANSFER_OPERATION) {

    override fun toBytes(): ByteArray {
        val feeBytes = fee.toBytes()
        val fromBytes = from.toBytes()
        val toBytes = to.toBytes()
        val amountBytes = amount.toBytes()
        return Bytes.concat(feeBytes, fromBytes, toBytes, amountBytes)
    }

    override fun toJsonString(): String? {
        val gson = GsonBuilder()
            .registerTypeAdapter(ContractTransferOperation::class.java, Serializer())
            .create()
        return gson.toJson(this)
    }

    override fun toJsonObject(): JsonElement {
        return JsonArray().apply {
            add(this@ContractTransferOperation.id)
            add(JsonObject().apply {
                add(KEY_FEE, fee.toJsonObject())
                addProperty(KEY_FROM, from.getObjectId())
                addProperty(KEY_TO, to.getObjectId())
                add(KEY_AMOUNT, amount.toJsonObject())
            })
        }
    }

    companion object {
        private const val KEY_FROM = "from"
        private const val KEY_TO = "to"
        private const val KEY_AMOUNT = "amount"
    }

    /**
     * Serializer used to build a json element from [ContractTransferOperation] instance
     */
    class Serializer : JsonSerializer<ContractTransferOperation> {
        override fun serialize(
            src: ContractTransferOperation?,
            typeOfSrc: Type?,
            context: JsonSerializationContext?
        ): JsonElement? {
            if (src == null) return null

            val arrayRep = JsonArray()
            arrayRep.add(src.id)
            arrayRep.add(src.toJsonObject())
            return arrayRep
        }
    }

    /**
     * Deserializer used to build a [ContractTransferOperation] instance from JSON
     */
    class Deserializer : JsonDeserializer<ContractTransferOperation> {

        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext
        ): ContractTransferOperation? {
            if (json == null || !json.isJsonObject) return null

            val jsonObject = json.asJsonObject

            val amount = context.deserialize<AssetAmount>(
                jsonObject.get(KEY_AMOUNT),
                AssetAmount::class.java
            )
            val fee = context.deserialize<AssetAmount>(
                jsonObject.get(KEY_FEE),
                AssetAmount::class.java
            )

            val from =
                Contract(jsonObject.get(KEY_FROM).asString)
            val to = Account(jsonObject.get(KEY_TO).asString)

            return ContractTransferOperation(fee, from, to, amount)
        }
    }
}
