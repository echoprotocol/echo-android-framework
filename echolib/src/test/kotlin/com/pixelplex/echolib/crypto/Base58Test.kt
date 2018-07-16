/*
 * Copyright 2011 Google Inc.
 * Copyright 2018 Andreas Schildbach
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

package com.pixelplex.echolib.crypto

import com.pixelplex.bitcoinj.AddressFormatException
import com.pixelplex.bitcoinj.Base58
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigInteger
import java.util.*

/**
 * Test cases for [Base58]
 *
 * @author Dmitriy Bushuev
 */
class Base58Test {

    @Test
    @Throws(Exception::class)
    fun testEncode() {
        val testbytes = "Hello World".toByteArray()
        assertEquals("JxF12TrwUP45BMd", Base58.encode(testbytes))

        val bi = BigInteger.valueOf(3471844090L)
        assertEquals("16Ho7Hs", Base58.encode(bi.toByteArray()))

        val zeroBytes1 = ByteArray(1)
        assertEquals("1", Base58.encode(zeroBytes1))

        val zeroBytes7 = ByteArray(7)
        assertEquals("1111111", Base58.encode(zeroBytes7))

        // test empty encode
        assertEquals("", Base58.encode(ByteArray(0)))
    }

    @Test
    @Throws(Exception::class)
    fun testEncodeChecked_privateKey() {
        val encoded = Base58.encodeChecked(128, ByteArray(32))
        assertEquals("5HpHagT65TZzG1PH3CSu63k8DbpvD8s5ip4nEB3kEsreAbuatmU", encoded)
    }

    @Test
    @Throws(Exception::class)
    fun testDecode() {
        val testbytes = "Hello World".toByteArray()
        val actualbytes = Base58.decode("JxF12TrwUP45BMd")
        assertTrue(String(actualbytes), Arrays.equals(testbytes, actualbytes))

        assertTrue("1", Arrays.equals(Base58.decode("1"), ByteArray(1)))
        assertTrue("1111", Arrays.equals(Base58.decode("1111"), ByteArray(4)))

        // Test decode of empty String.
        assertEquals(0, Base58.decode("").size)
    }

    @Test(expected = AddressFormatException::class)
    fun testDecode_invalidBase58() {
        Base58.decode("This isn't valid base58")
    }

    @Test
    fun testDecodeChecked() {
        Base58.decodeChecked("4stwEBjT6FYyVV")

        // Now check we can correctly decode the case where the high bit of the first byte is not zero, so BigInteger
        // sign extends. Fix for a bug that stopped us parsing keys exported using sipas patch.
        Base58.decodeChecked("93VYUMzRG9DdbRP72uQXjaWibbQwygnvaCu9DumcqDjGybD864T")
    }

    @Test(expected = AddressFormatException::class)
    fun decode_invalidCharacter_notInAlphabet() {
        Base58.decodeChecked("J0F12TrwUP45BMd")
    }

    @Test(expected = AddressFormatException::class)
    fun testDecodeChecked_invalidChecksum() {
        Base58.decodeChecked("4stwEBjT6FYyVW")
    }

    @Test(expected = AddressFormatException::class)
    fun testDecodeChecked_shortInput() {
        Base58.decodeChecked("4s")
    }

    @Test
    fun testDecodeToBigInteger() {
        val input = Base58.decode("129")
        assertEquals(BigInteger(1, input), Base58.decodeToBigInteger("129"))
    }

}
