package com.pixelplex.echolib.support

import com.pixelplex.bitcoinj.Base58
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Test cases for [Checksum]
 *
 * @author Dmitriy Bushuev
 */
class ChecksumTest {

    @Test
    fun checksumLengthTest() {
        val bytes = Base58.decode("JxF12TrwUP45BMd")

        val hashSizeInBytes = Checksum.CHECKSUM_SIZE
        val hash = Checksum.calculateChecksum(bytes)

        assertTrue(hash.size == hashSizeInBytes)
    }

}
