package com.pixelplex.echolib.model

import com.pixelplex.echolib.exception.MalformedAddressException
import org.bitcoinj.core.Base58
import org.bitcoinj.core.ECKey
import org.spongycastle.crypto.digests.RIPEMD160Digest

/**
 * Represents account model in Graphene blockchain
 * (@see https://bitshares.org/doxygen/classgraphene_1_1chain_1_1address.html)
 *
 * @author Daria Pechkovskaya
 */
class Address {

    var pubKey: PublicKey
        private set

    private val prefix: String

    @JvmOverloads
    constructor(pubKey: PublicKey, prefix: String = BITSHARES_PREFIX) {
        this.pubKey = pubKey
        this.prefix = prefix
    }

    constructor(address: String) {
        this.prefix = address.substring(0..PREFIX_SIZE)

        val decoded = Base58.decode(address.substring(PREFIX_SIZE))
        val pubKey = decoded.copyOfRange(0, decoded.size - CHECKSUM_SIZE)
        this.pubKey = PublicKey(ECKey.fromPublicOnly(pubKey))

        val calculatedChecksum = calculateChecksum(pubKey)
        val checksum = decoded.copyOfRange(decoded.size - CHECKSUM_SIZE, decoded.size)

        for ((i, data) in calculatedChecksum.withIndex()) {
            if (checksum[i] != data) {
                throw MalformedAddressException("Address checksum error")
            }
        }
    }

    private fun calculateChecksum(data: ByteArray): ByteArray {
        val checksum = ByteArray(CHECKSUM_SIZE)
        RIPEMD160Digest().apply {
            update(data, 0, data.size)
            doFinal(checksum, 0)
        }

        return checksum.copyOfRange(0, CHECKSUM_SIZE)
    }

    companion object {
        private const val HASH_LENGTH = 160
        private const val BYTES = 8

        const val BITSHARES_PREFIX = "GPH"
        const val PREFIX_SIZE = 3
        const val CHECKSUM_SIZE = HASH_LENGTH / BYTES
    }

}
