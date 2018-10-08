package org.echo.mobile.framework.support

import com.google.common.primitives.Bytes
import org.echo.mobile.bitcoinj.revert

/**
 * Contains extensions and types for serialization to bytes
 *
 * @author Daria Pechkovskaya
 */

/**
 * Serializes [String] value to byte array. Concatenates length of string in bytes and string bytes.
 */
fun String.serialize(): ByteArray = byteArrayOf(length.toByte()) + toByteArray()

/**
 * Serializes [Boolean] value to byte array by serialization type.
 * If value is true byte is 1 else 0.
 */
fun Boolean.serialize(): ByteArray = byteArrayOf(if (this) 1 else 0)

/**
 * Serializes typed [Array] to byte array by serialization function. Concatenates size of array and
 * byte array from serializing function.
 *
 * @param toBytes Function to serialize item of array
 */
fun <T> Array<T>.serialize(toBytes: (T) -> ByteArray): ByteArray {
    val sizeBytes = byteArrayOf(size.toByte())
    var bytes = byteArrayOf()
    forEach { item -> bytes += toBytes(item) }

    return Bytes.concat(sizeBytes, bytes)
}

/**
 * Serializes typed [Set] to byte array by serialization function. Concatenates size of set and
 * byte array from serializing function.
 *
 * @param toBytes Function to serialize item of set
 */
fun <T> Set<T>.serialize(toBytes: (T) -> ByteArray): ByteArray {
    val sizeBytes = byteArrayOf(size.toByte())
    var bytes = byteArrayOf()
    forEach { item -> bytes += toBytes(item) }

    return Bytes.concat(sizeBytes, bytes)
}

/**
 * Serializes typed [Map] to byte array by serialization functions. Concatenates size of map and
 * byte arrays from serializing functions.
 *
 * @param keyToBytes Function to serialize key item of map
 * @param valueToBytes Function to serialize value item of map
 */
fun <K, V> Map<K, V>.serialize(
    keyToBytes: (K) -> ByteArray,
    valueToBytes: (V) -> ByteArray
): ByteArray {
    val sizeBytes = byteArrayOf(size.toByte())
    var bytes = byteArrayOf()
    forEach { key, value ->
        bytes += keyToBytes(key)
        bytes += valueToBytes(value)
    }

    return Bytes.concat(sizeBytes, bytes)
}

/**
 * Encapsulates logic, associated with serialization of [Number] value to [ByteArray]
 */
interface IntType {

    /**
     * Serializes [Number] value to [ByteArray]
     */
    fun <T : Number> serialize(t: T): ByteArray
}

/**
 * Serializes [Number] value to [ByteArray] by int64 type
 */
object Int64 : IntType {
    override fun <T : Number> serialize(t: T): ByteArray = t.toLong().revert()
}

/**
 * Serializes [Number] value to [ByteArray] by uint8 type
 */
object Uint8 : IntType {
    override fun <T : Number> serialize(t: T): ByteArray = t.toLong().toUnsignedByteArray()
}

/**
 * Serializes [Number] value to [ByteArray] by uint16 type
 */
object Uint16 : IntType {
    override fun <T : Number> serialize(t: T): ByteArray = t.toShort().revert()
}

/**
 * Serializes [Number] value to [ByteArray] by uint32 type
 */
object Uint32 : IntType {
    override fun <T : Number> serialize(t: T): ByteArray = t.toInt().revert()
}
