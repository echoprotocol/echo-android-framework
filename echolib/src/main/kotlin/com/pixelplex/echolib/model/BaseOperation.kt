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
     * Required amount of fee [AssetAmount] to operation
     */
    abstract var fee: AssetAmount

}
