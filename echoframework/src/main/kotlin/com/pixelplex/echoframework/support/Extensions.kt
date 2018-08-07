@file:JvmName("Extensions")

package com.pixelplex.echoframework.support

import com.google.common.primitives.UnsignedLong
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.pixelplex.echoframework.TIME_DATE_FORMAT
import com.pixelplex.echoframework.support.crypto.Varint
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

/**
 * Contains all project extensions
 *
 * @author Daria Pechkovskaya
 * @author Bushuev Dmitriy
 */

/**
 * Format date to default date format
 *
 * @return formatted date text
 */
fun Date.format(): String = this.format(TIME_DATE_FORMAT)

/**
 * Format date to time format
 *
 * @param dateFormat: new date format
 * @return formatted date text
 */
fun Date.format(dateFormat: String): String {
    val dateFormatter = SimpleDateFormat(dateFormat, Locale.getDefault())
    dateFormatter.timeZone = TimeZone.getTimeZone("GMT")
    return dateFormatter.format(this)
}

/**
 * Parse string to [Date] according to required configuration
 */
@JvmOverloads
fun String.parse(
    format: String = TIME_DATE_FORMAT,
    timeZone: TimeZone = TimeZone.getTimeZone("UTC"),
    locale: Locale = Locale.getDefault(),
    default: Date? = null,
    catch: (e: Exception) -> Unit = {}
): Date? = try {
    val dateFormat = SimpleDateFormat(format, locale).apply {
        this.timeZone = timeZone
    }
    dateFormat.parse(this)
} catch (e: Exception) {
    catch(e)
    default
}

/**
 * Converts receiver string to json object with google json parser
 */
fun String.toJsonObject(): JsonObject? =
    try {
        JsonParser().parse(this).asJsonObject
    } catch (e: Exception) {
        null
    }

/**
 * Converts signed long value into unsigned
 */
fun Long.toUnsignedLong(): UnsignedLong = UnsignedLong.valueOf(this)

/**
 * Converts signed long value to unsigned and writes to byte array
 */
fun Long.toUnsignedByteArray(): ByteArray {
    val byteArrayOutputStream = ByteArrayOutputStream()
    DataOutputStream(byteArrayOutputStream).use { out ->
        Varint.writeUnsignedVarLong(this, out)
    }

    return byteArrayOutputStream.toByteArray()
}

private val hexArray = "0123456789abcdef".toCharArray()

/**
 * Converts an hexadecimal string to its corresponding byte[] value.
 *
 * @return: The actual byte array.
 */
fun String.hexToBytes(): ByteArray {
    val result = ByteArray(length / 2)

    for (i in 0 until length step 2) {
        val firstIndex = hexArray.indexOf(this[i])
        val secondIndex = hexArray.indexOf(this[i + 1])

        val octet = firstIndex.shl(4).or(secondIndex)
        result[i.shr(1)] = octet.toByte()
    }

    return result
}

/**
 * Return source byte array in reverse form
 */
fun ByteArray.revert(): ByteArray {
    val reverted = ByteArray(this.size)
    for (i in reverted.indices) {
        reverted[i] = this[this.size - i - 1]
    }
    return reverted
}

/**
 * Decodes an ascii string to a byte array.
 */
fun String.hexlify(): ByteArray {
    val buffer = ByteBuffer.allocate(this.length)
    for (letter in this.toCharArray()) {
        buffer.put(letter.toByte())
    }
    return buffer.array()
}

/**
 * Creates new [MessageDigest] for required [algorithm]
 */
private fun newDigest(algorithm: String) = MessageDigest.getInstance(algorithm)

/**
 * Creates sha256 hash from receiver byte array
 */
fun ByteArray.sha256hash(): ByteArray = newDigest("SHA-256").digest(this)

/**
 * Creates 512 hash from receiver byte array
 */
fun ByteArray.sha512hash(): ByteArray = newDigest("SHA-512").digest(this)

