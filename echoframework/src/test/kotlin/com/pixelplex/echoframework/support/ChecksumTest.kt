package com.pixelplex.echoframework.support

import com.pixelplex.bitcoinj.Base58
import com.pixelplex.echoframework.support.crypto.Checksum
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.spongycastle.util.encoders.Hex

/**
 * Test cases for [Checksum]
 *
 * @author Dmitriy Bushuev
 */
class ChecksumTest {

    @Test
    fun checksumLengthTest() {
        val checksum = checksum("JxF12TrwUP45BMd")
        val hashSizeInBytes = Checksum.CHECKSUM_SIZE

        assertTrue(checksum.size == hashSizeInBytes)
    }

    @Test
    fun checksumValidationTest() {
        val checksum = checksum("JxF12TrwUP45BMd")

        assertEquals(VALID_CHECKSUM_HEX, Hex.toHexString(checksum))
    }

    private fun checksum(source: String): ByteArray {
        val bytes = Base58.decode(source)

        return Checksum.calculateChecksum(bytes)
    }

    companion object {
        private const val VALID_CHECKSUM_HEX = "a830d7be"
    }

}
