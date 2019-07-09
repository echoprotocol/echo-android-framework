/*
 * Copyright 2011 Thilo Planz
 * Copyright 2014 Andreas Schildbach
 * Copyright 2017 Nicola Atzei
 * Copyright 2018 Dmitriy Bushuev
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

package org.echo.mobile.bitcoinj

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigInteger
import java.util.Arrays

/**
 * Test cases for crypto utils
 *
 * @author Dmitriy Bushuev
 */
class CryptoUtilsTest {

    @Test
    fun testReverseBytes() {
        assertArrayEquals(
            byteArrayOf(1, 2, 3, 4, 5),
            byteArrayOf(5, 4, 3, 2, 1).reverseBytes()
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun bigIntegerToBytes_convertNegativeNumber() {
        val b = BigInteger.valueOf(-1)
        b.bigIntegerToBytes(32)
    }

    @Test(expected = IllegalArgumentException::class)
    fun bigIntegerToBytes_convertWithNegativeLength() {
        val b = BigInteger.valueOf(10)
        b.bigIntegerToBytes(-1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun bigIntegerToBytes_convertWithZeroLength() {
        val b = BigInteger.valueOf(10)
        b.bigIntegerToBytes(0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun bigIntegerToBytes_insufficientLength() {
        val b = BigInteger.valueOf(2048)   // base 2
        b.bigIntegerToBytes(1)
    }

    @Test
    fun bigIntegerToBytes_convertZero() {
        val b = BigInteger.valueOf(0)
        val expected = byteArrayOf(0)
        val actual = b.bigIntegerToBytes(1)
        assertTrue(Arrays.equals(expected, actual))
    }

    @Test
    fun bigIntegerToBytes_singleByteSignFit() {
        val b = BigInteger.valueOf(15)
        val expected = byteArrayOf(15)
        val actual = b.bigIntegerToBytes(1)
        assertTrue(Arrays.equals(expected, actual))
    }

    @Test
    fun bigIntegerToBytes_paddedSingleByte() {
        val b = BigInteger.valueOf(15)
        val expected = byteArrayOf(0, 15)
        val actual = b.bigIntegerToBytes(2)
        assertTrue(Arrays.equals(expected, actual))
    }

    @Test
    fun bigIntegerToBytes_singleByteSignDoesNotFit() {
        val b = BigInteger.valueOf(128)     // 128 (2-compl does not fit in one byte)
        val expected = byteArrayOf(-128)                 // -128 == 1000_0000 (compl-2)
        val actual = b.bigIntegerToBytes(1)
        assertTrue(Arrays.equals(expected, actual))
    }

}
