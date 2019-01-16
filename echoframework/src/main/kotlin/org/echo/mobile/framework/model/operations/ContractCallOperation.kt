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
import org.echo.mobile.framework.model.Account
import org.echo.mobile.framework.model.AssetAmount
import org.echo.mobile.framework.model.BaseOperation
import org.echo.mobile.framework.model.contract.Contract
import org.echo.mobile.framework.model.contract.ContractResult
import org.echo.mobile.framework.support.Int64
import org.echo.mobile.framework.support.Uint8
import java.lang.reflect.Type

/**
 * Represents blockchain operation for calling contract w2ith changing state
 *
 * @author Daria Pechkovskaya
 * @author Dmitriy Bushuev
 */
class ContractCallOperation @JvmOverloads constructor(
    var registrar: Account,
    var callee: Contract,
    val value: AssetAmount,
    val gasPrice: UnsignedLong,
    val gas: UnsignedLong,
    val code: String,
    override var fee: AssetAmount = AssetAmount(UnsignedLong.ZERO)
) : BaseOperation(OperationType.CONTRACT_CALL_OPERATION) {

    var contractResult: ContractResult? = null

    override fun toBytes(): ByteArray {
        val feeBytes = fee.toBytes()
        val registrarBytes = registrar.toBytes()
        val contractBytes = callee.toBytes()
        val valueBytes = value.toBytes()
        val gasPriceBytes = Int64.serialize(gasPrice)
        val gasBytes = Int64.serialize(gas)
        val codeBytes = Uint8.serialize(code.length) + code.toByteArray()

        return Bytes.concat(
            feeBytes, registrarBytes, valueBytes, gasPriceBytes, gasBytes, codeBytes, contractBytes
        )
    }

    override fun toJsonString(): String? {
        val gson = GsonBuilder()
            .registerTypeAdapter(ContractCallOperation::class.java, Serializer())
            .create()
        return gson.toJson(this)
    }

    override fun toJsonObject(): JsonElement {
        return JsonArray().apply {
            add(this@ContractCallOperation.id)
            add(JsonObject().apply {
                add(KEY_FEE, fee.toJsonObject())
                addProperty(KEY_REGISTRAR, registrar.getObjectId())
                addProperty(KEY_RECEIVER, callee.getObjectId())
                add(KEY_VALUE, value.toJsonObject())
                addProperty(KEY_GAS_PRICE, gasPrice)
                addProperty(KEY_GAS, gas)
                addProperty(KEY_CODE, code)
            })
        }
    }

    override fun toString(): String = "${javaClass.simpleName}(${toJsonObject()})"

    companion object {
        private const val KEY_REGISTRAR = "registrar"
        private const val KEY_RECEIVER = "callee"
        private const val KEY_VALUE = "value"
        private const val KEY_GAS_PRICE = "gasPrice"
        private const val KEY_GAS = "gas"
        private const val KEY_CODE = "code"
    }

    /**
     * Serializer used to build a json element from [ContractCallOperation] instance
     */
    class Serializer : JsonSerializer<ContractCallOperation> {
        override fun serialize(
            src: ContractCallOperation?,
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
     * Deserializer used to build a [ContractCallOperation] instance from JSON
     */
    class Deserializer : JsonDeserializer<ContractCallOperation> {

        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext
        ): ContractCallOperation? {
            if (json == null || !json.isJsonObject) return null

            val jsonObject = json.asJsonObject

            val fee = context.deserialize<AssetAmount>(
                jsonObject.get(KEY_FEE),
                AssetAmount::class.java
            )

            val registrar = Account(jsonObject.get(KEY_REGISTRAR).asString)
            val receiver: Contract? = jsonObject.get(KEY_RECEIVER)?.let { element ->
                Contract(element.asString)
            }
            val value = context.deserialize<AssetAmount>(
                jsonObject.get(KEY_VALUE),
                AssetAmount::class.java
            )
            val gasPrice = jsonObject.get(KEY_GAS_PRICE).asLong
            val gas = jsonObject.get(KEY_GAS).asLong
            val code = jsonObject.get(KEY_CODE).asString

            return ContractCallOperation(
                registrar,
                receiver!!,
                value,
                UnsignedLong.valueOf(gasPrice),
                UnsignedLong.valueOf(gas),
                code,
                fee
            )
        }
    }
}
