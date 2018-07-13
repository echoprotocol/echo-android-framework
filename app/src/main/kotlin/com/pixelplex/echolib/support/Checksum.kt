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

    const val CHECKSUM_SIZE = HASH_LENGTH / BYTES

    /**
     * Calculates checksum
     */
    @JvmStatic
    fun calculateChecksum(data: ByteArray): ByteArray {
        val checksum = ByteArray(CHECKSUM_SIZE)
        RIPEMD160Digest().apply {
            update(data, 0, data.size)
            doFinal(checksum, 0)
        }

        return checksum.copyOfRange(0, CHECKSUM_SIZE)
    }

}
