/*
 * Copyright 2011 Google Inc.
 * Copyright 2014 Andreas Schildbach
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

package com.pixelplex.bitcoinj

import com.google.common.collect.Lists
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import org.junit.Assert.*
import org.junit.Test
import java.math.BigInteger
import java.util.concurrent.Callable
import java.util.concurrent.Executors


/**
 * Test cases for [ECKey]
 *
 * @author Dmitry Bushuev
 */
class ECKeyTest {

    @Test
    fun testPublicKeysAreEqual() {
        val key = ECKey()
        val pubKey1 = ECKey.fromPublicOnly(key.pubKeyPoint)
        assertTrue(pubKey1.isCompressed)
        val pubKey2 = pubKey1.decompress()
        assertEquals(pubKey1, pubKey2)
        assertEquals(pubKey1.hashCode(), pubKey2.hashCode())
    }

    @Test(expected = IllegalArgumentException::class)
    fun fromPrivate_exceedsSize() {
        val bytes = ByteArray(33)
        bytes[0] = 42
        ECKey.fromPrivate(bytes)
    }

    @Test
    @Throws(Exception::class)
    fun sValue() {
        // Check that we never generate an S value that is larger than half the curve order. This avoids a malleability
        // issue that can allow someone to change a transaction [hash] without invalidating the signature.
        val nThreads = 10
        val executor = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(nThreads))
        val sigFutures = Lists.newArrayList<ListenableFuture<ECDSASignature>>()
        val key = ECKey()
        for (i in 0 until nThreads) {
            val hash = Sha256Hash.of(byteArrayOf(i.toByte()))
            sigFutures.add(executor.submit(Callable<ECDSASignature> { key.sign(hash) }))
        }
        val sigs = Futures.allAsList<ECDSASignature>(sigFutures).get()
        for (signature in sigs) {
            assertTrue(signature.isCanonical)
        }
        val first = sigs[0]
        val duplicate = ECDSASignature(first.r, first.s)
        assertEquals(first, duplicate)
        assertEquals(first.hashCode(), duplicate.hashCode())

        val highS = ECDSASignature(first.r, ECKey.curve.n.subtract(first.s))
        assertFalse(highS.isCanonical)
    }

    @Test
    fun findRecoveryId() {
        var key = ECKey()
        val message = "Hello World!"
        val hash = Sha256Hash.of(message.toByteArray())
        val sig = key.sign(hash)
        key = ECKey.fromPublicOnly(key.pubKeyPoint)

        val possibleRecIds = Lists.newArrayList(0.toByte(), 1.toByte(), 2.toByte(), 3.toByte())
        val recId = key.findRecoveryId(hash, sig)
        assertTrue(possibleRecIds.contains(recId))
    }

    @Test
    fun keyRecoveryTestVector() {
        // a test that exercises key recovery with findRecoveryId() on a test vector
        // test vector from https://crypto.stackexchange.com/a/41339
        var key = ECKey.fromPrivate(
            BigInteger("ebb2c082fd7727890a28ac82f6bdf97bad8de9f5d7c9028692de1a255cad3e0f", 16)
        )
        val message = "Maarten Bodewes generated this test vector on 2016-11-08"
        val hash = Sha256Hash.of(message.toByteArray())
        val sig = key.sign(hash)
        key = ECKey.fromPublicOnly(key.pubKeyPoint)

        val recId = key.findRecoveryId(hash, sig)
        val expectedRecId: Byte = 0
        assertEquals(recId, expectedRecId)

        val pubKey = ECKey.fromPublicOnly(key.pubKeyPoint)
        val recoveredKey = ECKey.recoverFromSignature(recId.toInt(), sig, hash, true)
        assertEquals(recoveredKey, pubKey)
    }

    @Test
    @Throws(Exception::class)
    fun keyRecoveryWithFindRecoveryId() {
        val key = ECKey()
        val message = "Hello World!"
        val hash = Sha256Hash.of(message.toByteArray())
        val sig = key.sign(hash)

        val recId = key.findRecoveryId(hash, sig)
        val pubKey = ECKey.fromPublicOnly(key.pubKeyPoint)
        val recoveredKey = ECKey.recoverFromSignature(recId.toInt(), sig, hash, true)
        assertEquals(recoveredKey, pubKey)
    }

    @Test
    @Throws(Exception::class)
    fun keyRecovery() {
        var key = ECKey()
        val message = "Hello World!"
        val hash = Sha256Hash.of(message.toByteArray())
        val sig = key.sign(hash)
        key = ECKey.fromPublicOnly(key.pubKeyPoint)
        var found = false
        for (i in 0..3) {
            val key2 = ECKey.recoverFromSignature(i, sig, hash, true)
            checkNotNull(key2)
            if (key == key2) {
                found = true
                break
            }
        }
        assertTrue(found)
    }

    @Test
    @Throws(Exception::class)
    fun testGetPrivateKeyAsHex() {
        val key = ECKey.fromPrivate(BigInteger.TEN).decompress() // An example private key.
        assertEquals(
            "000000000000000000000000000000000000000000000000000000000000000a",
            key.getPrivateKeyAsHex()
        )
    }

    @Test
    @Throws(Exception::class)
    fun testCreatedSigAndPubkeyAreCanonical() {
        // Tests that we will not generate non-canonical pubkeys
        val key = ECKey()
        assertTrue(ECKey.isPubKeyCanonical(key.pubKey))
    }

}
