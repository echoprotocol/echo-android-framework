/*
 * Copyright 2011 Google Inc.
 * Copyright 2014 Andreas Schildbach
 * Copyright 2018 Bushuev Dmitriy and Dasha Pechkovskaya
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:JvmName("Utils")

package org.echo.mobile.bitcoinj

import com.google.common.base.Preconditions
import com.google.common.io.BaseEncoding
import java.math.BigInteger
import java.nio.ByteBuffer

/**
 * A collection of various utility methods that are helpful for working with the Bitcoin protocol.
 * To enable debug logging from the library, run with -Dbitcoinj.logging=true on your command line.
 *
 * @author Daria Pechkovskaya
 */

private var isAndroid = -1
val isAndroidRuntime: Boolean
    get() {
        if (isAndroid == -1) {
            val runtime = System.getProperty("java.runtime.name")
            isAndroid = if (runtime != null && runtime == "Android Runtime") 1 else 0
        }
        return isAndroid == 1
    }

val HEX: BaseEncoding = BaseEncoding.base16().lowerCase()

/**
 * The regular [BigInteger.toByteArray] includes the sign bit of the number and
 * might result in an extra byte addition. This method removes this extra byte.
 *
 * Assuming only positive numbers, it's possible to discriminate if an extra byte
 * is added by checking if the first element of the array is 0 (0000_0000).
 * Due to the minimal representation provided by BigInteger, it means that the bit sign
 * is the least significant bit 0000_000**0** .
 * Otherwise the representation is not minimal.
 * For example, if the sign bit is 0000_00**0**0, then the representation is not minimal due to the rightmost zero.
 *
 * @param numBytes the desired size of the resulting byte array
 */
fun BigInteger.bigIntegerToBytes(numBytes: Int): ByteArray {
    Preconditions.checkArgument(this.signum() >= 0, "b must be positive or zero")
    Preconditions.checkArgument(numBytes > 0, "numBytes must be positive")
    val src = this.toByteArray()
    val dest = ByteArray(numBytes)
    val isFirstByteOnlyForSign = src[0].toInt() == 0
    val length = if (isFirstByteOnlyForSign) src.size - 1 else src.size
    Preconditions.checkArgument(
        length <= numBytes,
        "The given number does not fit in $numBytes"
    )
    val srcPos = if (isFirstByteOnlyForSign) 1 else 0
    val destPos = numBytes - length
    System.arraycopy(src, srcPos, dest, destPos, length)
    return dest
}

/**
 * Returns a copy of the given byte array in reverse order.
 */
fun ByteArray.reverseBytes(): ByteArray {
    // We could use the XOR trick here but it's easier to understand if we don't. If we find this is really a
    // performance issue the matter can be revisited.
    val buf = ByteArray(this.size)
    for (i in this.indices)
        buf[i] = this[this.size - 1 - i]
    return buf
}

/**
 * Returns an array of bytes with the underlying data used to represent an integer in the reverse form.
 * This is useful for endianess switches, meaning that if you give this function a big-endian integer
 * it will return it's little-endian bytes.
 * @param input An Integer value.
 * @return The array of bytes that represent this value in the reverse format.
 */
fun Int.revert(): ByteArray =
    ByteBuffer.allocate(Integer.SIZE / 8).putInt(Integer.reverseBytes(this)).array()

/**
 * Same operation as in the revertInteger function, but in this case for a short (2 bytes) value.
 * @param input A Short value
 * @return The array of bytes that represent this value in the reverse format.
 */
fun Short.revert(): ByteArray =
    ByteBuffer.allocate(java.lang.Short.SIZE / 8)
        .putShort(java.lang.Short.reverseBytes(this)).array()


/**
 * Same operation as in the revertInteger function, but in this case for a long (8 bytes) value.
 * @param input A Long value
 * @return The array of bytes that represent this value in the reverse format.
 */
fun Long.revert(): ByteArray =
    ByteBuffer.allocate(java.lang.Long.SIZE / 8)
        .putLong(java.lang.Long.reverseBytes(this)).array()

