package org.echo.mobile.framework.model.operations

import com.google.common.primitives.Bytes
import com.google.common.primitives.UnsignedLong
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import org.echo.mobile.framework.model.AssetAmount
import org.echo.mobile.framework.model.BaseOperation
import org.echo.mobile.framework.model.GrapheneObject
import org.echo.mobile.framework.model.contract.Contract
import org.echo.mobile.framework.support.serialize
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
 *  @param fee         Fee to pay.
 *  @param caller      Contract to transfer asset from.
 *  @param callee      Account or contract to transfer asset to.
 *  @param value       The amount of asset to transfer from from to to.
 *  @param method      Called method
 */
@JvmOverloads
constructor(
    override var fee: AssetAmount = AssetAmount(UnsignedLong.ZERO),
    val caller: Contract,
    var callee: GrapheneObject,
    val method: String,
    val value: AssetAmount
) : BaseOperation(OperationType.CONTRACT_INTERNAL_CALL_OPERATION) {

    override fun toBytes(): ByteArray {
        val fromBytes = caller.toBytes()
        val toBytes = callee.toBytes()
        val methodBytes = method.serialize()
        val amountBytes = value.toBytes()
        return Bytes.concat(fromBytes, toBytes, methodBytes, amountBytes)
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
                addProperty(KEY_CALLER, caller.getObjectId())
                addProperty(KEY_CALLEE, callee.getObjectId())
                add(KEY_VALUE, value.toJsonObject())
                addProperty(KEY_METHOD, method)
            })
        }
    }

    companion object {
        private const val KEY_CALLER = "caller"
        private const val KEY_CALLEE = "callee"
        private const val KEY_VALUE = "value"
        private const val KEY_METHOD = "method"
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
                jsonObject.get(KEY_VALUE),
                AssetAmount::class.java
            )
            val method = jsonObject.get(KEY_METHOD).asString

            val from =
                Contract(jsonObject.get(KEY_CALLER).asString)
            val to = GrapheneObject(jsonObject.get(KEY_CALLEE).asString)

            return ContractTransferOperation(
                AssetAmount(UnsignedLong.ZERO),
                from,
                to,
                method,
                amount
            )
        }
    }
}
