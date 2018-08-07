package com.pixelplex.echoframework.model.operations

import com.google.common.primitives.UnsignedLong
import com.google.gson.*
import com.pixelplex.bitcoinj.revert
import com.pixelplex.echoframework.model.Account
import com.pixelplex.echoframework.model.Asset
import com.pixelplex.echoframework.model.AssetAmount
import com.pixelplex.echoframework.model.BaseOperation
import com.pixelplex.echoframework.model.contract.Contract
import com.pixelplex.echoframework.support.toUnsignedByteArray
import java.lang.reflect.Type

/**
 * Represents blockchain operation for working with contract.
 *
 * @author Daria Pechkovskaya
 */
class ContractOperation
@JvmOverloads constructor(
    val registrar: Account,
    val receiver: Contract? = null,
    val asset: Asset,
    val value: UnsignedLong,
    val gasPrice: UnsignedLong,
    val gas: UnsignedLong,
    val code: String,
    override var fee: AssetAmount = AssetAmount(UnsignedLong.ZERO)

) : BaseOperation(OperationType.CONTRACT_OPERATION) {

    override fun toBytes(): ByteArray {
        val feeBytes = fee.toBytes()
        val registrarBytes = registrar.toBytes()

        var hasContract: ByteArray = byteArrayOf(0)
        var contractBytes: ByteArray? = null
        receiver?.let { nonNullContract ->
            hasContract = byteArrayOf(1)
            contractBytes = nonNullContract.toBytes()
        }

        val assetIdBytes = asset.instance.toUnsignedByteArray()
        val valueBytes = value.toLong().revert()
        val gasPriceBytes = gasPrice.toLong().revert()
        val gasBytes = gas.toLong().revert()
        val codeLengthBytes = code.length.toLong().toUnsignedByteArray()
        val codeBytes = code.toByteArray()

        return if (contractBytes != null) {
            feeBytes + registrarBytes + hasContract + contractBytes!! + assetIdBytes +
                    valueBytes + gasPriceBytes + gasBytes + codeLengthBytes + codeBytes
        } else {
            feeBytes + registrarBytes + hasContract + assetIdBytes + valueBytes +
                    gasPriceBytes + gasBytes + codeLengthBytes + codeBytes
        }
    }

    override fun toJsonString(): String? {
        val gson = GsonBuilder()
            .registerTypeAdapter(ContractOperation::class.java, Serializer())
            .create()
        return gson.toJson(this)
    }

    override fun toJsonObject(): JsonElement {
        return JsonArray().apply {
            add(this@ContractOperation.id)
            add(JsonObject().apply {
                add(KEY_FEE, fee.toJsonObject())
                addProperty(KEY_REGISTRAR, registrar.getObjectId())
                receiver?.let { contract ->
                    addProperty(KEY_RECEIVER, contract.getObjectId())
                }
                addProperty(KEY_ASSET_ID, asset.getObjectId())
                addProperty(KEY_VALUE, value)
                addProperty(KEY_GAS_PRICE, gasPrice)
                addProperty(KEY_GAS, gas)
                addProperty(KEY_CODE, code)
            })
        }
    }

    companion object {
        private const val KEY_REGISTRAR = "registrar"
        private const val KEY_RECEIVER = "receiver"
        private const val KEY_VALUE = "value"
        private const val KEY_GAS_PRICE = "gasPrice"
        private const val KEY_GAS = "gas"
        private const val KEY_CODE = "code"
        private const val KEY_ASSET_ID = "asset_id"
    }

    /**
     * Serializer used to build a json element from [ContractOperation] instance
     */
    class Serializer : JsonSerializer<ContractOperation> {
        override fun serialize(
            src: ContractOperation?,
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
     * Deserializer used to build a [ContractOperation] instance from JSON
     */
    class Deserializer : JsonDeserializer<ContractOperation> {

        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext
        ): ContractOperation? {

            if (json == null || !json.isJsonObject) return null

            val jsonObject = json.asJsonObject

            val fee = context.deserialize<AssetAmount>(
                jsonObject.get(KEY_FEE),
                AssetAmount::class.java
            )

            val registrar = Account(jsonObject.get(KEY_REGISTRAR).asString)
            val receiver =
                Contract(jsonObject.get(KEY_RECEIVER).asString)
            val assetId = Asset(jsonObject.get(KEY_ASSET_ID).asString)
            val value = jsonObject.get(KEY_VALUE).asLong
            val gasPrice = jsonObject.get(KEY_GAS_PRICE).asLong
            val gas = jsonObject.get(KEY_GAS).asLong
            val code = jsonObject.get(KEY_CODE).asString

            return ContractOperation(
                registrar,
                receiver,
                assetId,
                UnsignedLong.valueOf(value),
                UnsignedLong.valueOf(gasPrice),
                UnsignedLong.valueOf(gas),
                code,
                fee
            )
        }
    }
}
