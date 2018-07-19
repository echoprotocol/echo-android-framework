package com.pixelplex.echolib.model

import com.google.gson.JsonElement

/**
 * Container template class used whenever we have an optional field.
 *
 * The idea here is that the binary serialization of this field should be performed
 * in a specific way determined by the field implementing the {@link ByteSerializable}
 * interface, more specifically using the {@link ByteSerializable#toBytes()} method.
 *
 * However, if the field is missing, the Optional class should be able to know how
 * to serialize it, as this is always done by placing an zero byte.
 *
 * @author Daria Pechkovskaya
 */
class Optional<T : GrapheneSerializable>(private val field: T?) : GrapheneSerializable {

    val isSet: Boolean
        get() = this.field != null

    override fun toBytes(): ByteArray =
        try {
            field?.toBytes() ?: byteArrayOf(0.toByte())
        } catch (e: Exception) {
            e.printStackTrace()
            byteArrayOf()
        }

    override fun toJsonString(): String? = field?.toJsonString()

    override fun toJsonObject(): JsonElement? = field?.toJsonObject()
}
