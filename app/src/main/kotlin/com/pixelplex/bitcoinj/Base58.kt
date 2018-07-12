/*
 * Copyright 2011 Google Inc.
 * Copyright 2018 Andreas Schildbach
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

package com.pixelplex.bitcoinj

import java.util.*

/**
 * Base58 is a way to encode Bitcoin addresses (or arbitrary data) as alphanumeric strings.
 * <p>
 * Note that this is not the same base58 as used by Flickr, which you may find referenced around the Internet.
 * <p>
 * You may want to consider working with {@link PrefixedChecksummedBytes} instead, which
 * adds support for testing the prefix and suffix bytes commonly found in addresses.
 * <p>
 * Satoshi explains: why base-58 instead of standard base-64 encoding?
 * <ul>
 * <li>Don't want 0OIl characters that look the same in some fonts and
 *     could be used to create visually identical looking account numbers.</li>
 * <li>A string with non-alphanumeric characters is not as easily accepted as an account number.</li>
 * <li>E-mail usually won't line-break if there's no punctuation to break at.</li>
 * <li>Doubleclicking selects the whole number as one word if it's all alphanumeric.</li>
 * </ul>
 * <p>
 * However, note that the encoding/decoding runs in O(n&sup2;) time, so it is not useful for large data.
 * <p>
 * The basic idea of the encoding is to treat the data bytes as a large number represented using
 * base-256 digits, convert the number to be represented using base-58 digits, preserve the exact
 * number of leading zeros (which are otherwise lost during the mathematical operations on the
 * numbers), and finally represent the resulting base-58 digits as alphanumeric ASCII characters.
 *
 *
 * @author Daria Pechkovskaya
 */
object Base58 {

    private val alphabet =
        "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".toCharArray()
    private val ENCODED_ZERO = alphabet[0]

    private const val INDEXES_ARRAY_SIZE = 128
    private val indexes = IntArray(INDEXES_ARRAY_SIZE)

    private const val DIVISION_BASE = 256
    private const val DIVISOR = 58

    private const val ELEMENT_MASK = 0xFF

    init {
        indexes.fill(-1)
        alphabet.forEachIndexed { i, c ->
            indexes[c.toInt()] = i
        }
    }

    /**
     * Encodes the given bytes as a base58 string (no checksum is appended).
     *
     * @param input the bytes to encode
     * @return the base58-encoded string
     */
    fun encode(input: ByteArray): String {
        if (input.isEmpty()) return ""

        // Count leading zeros.
        val firstZero = input.indexOfFirst { item -> item.toInt() == 0 }
        var zeros = if (firstZero == -1) 0 else firstZero

        // Convert base-256 digits to base-58 digits (plus conversion to ASCII characters)
        val mutableInput = input.copyOf() // since we modify it in-place
        val encoded = CharArray(mutableInput.size * 2) // upper bound
        var outputStart = encoded.size
        var inputStart = zeros
        while (inputStart < mutableInput.size) {
            encoded[--outputStart] = alphabet[divmod(
                mutableInput,
                inputStart,
                DIVISION_BASE,
                DIVISOR
            ).toInt()]
            if (mutableInput[inputStart].toInt() == 0) {
                ++inputStart // optimization - skip leading zeros
            }
        }
        // Preserve exactly as many leading encoded zeros in output as there were leading zeros in input.
        while (outputStart < encoded.size && encoded[outputStart] == ENCODED_ZERO) {
            ++outputStart
        }
        while (--zeros >= 0) {
            encoded[--outputStart] = ENCODED_ZERO
        }
        // Return encoded string (including encoded leading zeros).
        return String(encoded, outputStart, encoded.size - outputStart)
    }

    /**
     * Decodes the given base58 string into the original data bytes.
     *
     * @param input the base58-encoded string to decode
     * @return the decoded data bytes
     * @throws AddressFormatException if the given string is not a valid base58 string
     */
    @Throws(AddressFormatException::class)
    fun decode(input: String): ByteArray {
        if (input.isEmpty()) {
            return ByteArray(0)
        }
        // Convert the base58-encoded ASCII chars to a base58 byte sequence (base58 digits).
        val input58 = ByteArray(input.length)
        for (i in 0 until input.length) {
            val c = input[i]
            val digit = if (c.toInt() < 128) indexes[c.toInt()] else -1
            if (digit < 0) {
                throw AddressFormatException("Illegal character $c at position $i")
            }
            input58[i] = digit.toByte()
        }
        // Count leading zeros.
        var zeros = 0
        while (zeros < input58.size && input58[zeros].toInt() == 0) {
            ++zeros
        }
        // Convert base-58 digits to base-256 digits.
        val decoded = ByteArray(input.length)
        var outputStart = decoded.size
        var inputStart = zeros
        while (inputStart < input58.size) {
            decoded[--outputStart] = divmod(input58, inputStart, 58, 256)
            if (input58[inputStart].toInt() == 0) {
                ++inputStart // optimization - skip leading zeros
            }
        }
        // Ignore extra leading zeroes that were added during the calculation.
        while (outputStart < decoded.size && decoded[outputStart].toInt() == 0) {
            ++outputStart
        }
        // Return decoded data (including original number of leading zeros).
        return Arrays.copyOfRange(decoded, outputStart - zeros, decoded.size)
    }

    /**
     * Divides a number, represented as an array of bytes each containing a single digit
     * in the specified base, by the given divisor. The given number is modified in-place
     * to contain the quotient, and the return value is the remainder.
     *
     * @param number     the number to divide
     * @param firstDigit the index within the array of the first non-zero digit
     * (this is used for optimization by skipping the leading zeros)
     * @param base       the base in which the number's digits are represented (up to 256)
     * @param divisor    the number to divide by (up to 256)
     * @return the remainder of the division operation
     */
    private fun divmod(number: ByteArray, firstDigit: Int, base: Int, divisor: Int): Byte {
        // this is just long division which accounts for the base of the input digits
        var remainder = 0
        for (i in firstDigit until number.size) {
            val digit = number[i].toInt() and ELEMENT_MASK
            val temp = remainder * base + digit
            number[i] = (temp / divisor).toByte()
            remainder = temp % divisor
        }
        return remainder.toByte()
    }

}
