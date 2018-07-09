package com.pixelplex.echolib.model

import com.google.gson.JsonArray
import com.google.gson.JsonElement

/**
 * Represents base operation model in Graphene blockchain
 *
 *Created by Dasha on 09.07.2018
 */
abstract class BaseOperation : ByteSerializable, JsonSerializable {

    protected var type: OperationType

    protected var extensions: Extensions = Extensions()

    constructor(type: OperationType) {
        this.type = type
    }

    val id: Byte
        get() {
            return type.ordinal.toByte()
        }

    abstract fun setFee(assetAmount: AssetAmount)

    override fun toJsonObject(): JsonElement =
        JsonArray().apply {
            add(id)
        }
}