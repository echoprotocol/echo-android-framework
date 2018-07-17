package com.pixelplex.echolib.support

import org.spongycastle.crypto.digests.RIPEMD160Digest

/**
 * Contains logic of checksum calculation
 *
 * @author Dmitriy Bushuev
 */
object Checksum {

    private const val HASH_LENGTH = 160
    private const val BYTES = 8

    private const val CHECKSUM_BYTES_SIZE = HASH_LENGTH / BYTES
    const val CHECKSUM_SIZE = 4

    /**
     * Calculates checksum
     */
    @JvmStatic
    fun calculateChecksum(data: ByteArray): ByteArray {
        val checksum = ByteArray(CHECKSUM_BYTES_SIZE)
        RIPEMD160Digest().apply {
            update(data, 0, data.size)
            doFinal(checksum, 0)
        }

        return checksum.copyOfRange(0, CHECKSUM_SIZE)
    }

}
