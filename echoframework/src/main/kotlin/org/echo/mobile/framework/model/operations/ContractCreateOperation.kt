package org.echo.mobile.framework.model.operations

import com.google.common.primitives.Bytes
import com.google.common.primitives.UnsignedLong
import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import org.echo.mobile.framework.model.Account
import org.echo.mobile.framework.model.Asset
import org.echo.mobile.framework.model.AssetAmount
import org.echo.mobile.framework.support.Int64
import org.echo.mobile.framework.support.Uint8
import org.echo.mobile.framework.support.serialize
import java.lang.reflect.Type

/**
 * Represents blockchain operation for creating new smart contract
 *
 * @author Daria Pechkovskaya
 * @author Dmitriy Bushuev
 */
class ContractCreateOperation @JvmOverloads constructor(
    registrar: Account,
    value: AssetAmount,
    gasPrice: UnsignedLong,
    gas: UnsignedLong,
    code: String,
    var echAccuracy: Boolean = false,
    var supportedAsset: Asset? = null,
    fee: AssetAmount = AssetAmount(UnsignedLong.ZERO)
) : ContractOperation(
    registrar,
    value,
    gasPrice,
    gas,
    code,
    fee,
    OperationType.CONTRACT_CREATE_OPERATION
) {

    override fun toBytes(): ByteArray {
        val feeBytes = fee.toBytes()
        val registrarBytes = registrar.toBytes()
        val valueBytes = value.toBytes()
        val gasPriceBytes = Int64.serialize(gasPrice)
        val gasBytes = Int64.serialize(gas)
        val codeBytes = Uint8.serialize(code.length) + code.toByteArray()
        val echAccuracyBytes = echAccuracy.serialize()
        val supportedAssetBytes = supportedAsset?.let {
            byteArrayOf(1) + supportedAsset!!.toBytes()
        } ?: byteArrayOf(0)

        return Bytes.concat(
            feeBytes,
            registrarBytes,
            valueBytes,
            gasPriceBytes,
            gasBytes,
            codeBytes,
            echAccuracyBytes,
            supportedAssetBytes
        )
    }

    override fun toJsonObject(): JsonElement {
        return JsonArray().apply {
            add(this@ContractCreateOperation.id)
            add(JsonObject().apply {
                add(KEY_FEE, fee.toJsonObject())
                addProperty(KEY_REGISTRAR, registrar.getObjectId())
                add(KEY_VALUE, value.toJsonObject())
                addProperty(KEY_GAS_PRICE, gasPrice)
                addProperty(KEY_GAS, gas)
                addProperty(KEY_CODE, code)
                addProperty(KEY_ACCURACY, echAccuracy.toString())
                supportedAsset?.let {
                    addProperty(
                        KEY_SUPPORTED_ASSET,
                        supportedAsset!!.getObjectId()
                    )
                }
            })
        }
    }

    /**
     * Serializer used to build a json element from [ContractCreateOperation] instance
     */
    class Serializer : JsonSerializer<ContractCreateOperation> {
        override fun serialize(
            src: ContractCreateOperation?,
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
     * Deserializer used to build a [ContractCreateOperation] instance from JSON
     */
    class Deserializer : JsonDeserializer<ContractCreateOperation> {

        override fun deserialize(
            json: JsonElement?,
            typeOfT: Type?,
            context: JsonDeserializationContext
        ): ContractCreateOperation? {
            if (json == null || !json.isJsonObject) return null

            val jsonObject = json.asJsonObject

            val fee = context.deserialize<AssetAmount>(
                jsonObject.get(KEY_FEE),
                AssetAmount::class.java
            )

            val registrar = Account(jsonObject.get(KEY_REGISTRAR).asString)
            val value = context.deserialize<AssetAmount>(
                jsonObject.get(KEY_VALUE),
                AssetAmount::class.java
            )
            val gasPrice = jsonObject.get(KEY_GAS_PRICE).asLong
            val gas = jsonObject.get(KEY_GAS).asLong
            val code = jsonObject.get(KEY_CODE).asString
            val echAccuracy = jsonObject.get(KEY_ACCURACY).asBoolean

            val supportedAssetId = jsonObject.get(KEY_SUPPORTED_ASSET)?.asString
            val supportedAsset = supportedAssetId?.let { Asset(supportedAssetId) }

            return ContractCreateOperation(
                registrar,
                value,
                UnsignedLong.valueOf(gasPrice),
                UnsignedLong.valueOf(gas),
                code,
                echAccuracy,
                supportedAsset,
                fee
            )
        }
    }
}
