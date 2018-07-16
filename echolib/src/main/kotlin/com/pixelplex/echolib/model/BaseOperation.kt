package com.pixelplex.echolib.model

import com.google.gson.JsonArray
import com.google.gson.JsonElement

/**
 * Represents base operation model in Graphene blockchain
 *
 * @author Daria Pechkovskaya
 */
abstract class BaseOperation(protected var type: OperationType) : ByteSerializable,
    JsonSerializable {

    protected var extensions: Extensions = Extensions()

    val id: Byte
        get() = type.ordinal.toByte()

    override fun toJsonObject(): JsonElement =
        JsonArray().apply {
            add(id)
        }

    /**
     * Apply required amount of fee [assetAmount] to operation
     */
    abstract fun setFee(assetAmount: AssetAmount)

}
