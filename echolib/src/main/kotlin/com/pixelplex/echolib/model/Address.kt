package com.pixelplex.echolib.model

import com.google.common.primitives.Bytes
import com.pixelplex.bitcoinj.Base58
import com.pixelplex.bitcoinj.ECKey
import com.pixelplex.echolib.BuildConfig
import com.pixelplex.echolib.exception.MalformedAddressException
import com.pixelplex.echolib.support.Checksum.CHECKSUM_SIZE
import com.pixelplex.echolib.support.Checksum.calculateChecksum

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
    constructor(
        pubKey: PublicKey,
        prefix: String = if (BuildConfig.DEBUG) TESTNET_PREFIX else BITSHARES_PREFIX
    ) {
        this.pubKey = pubKey
        this.prefix = prefix
    }

    constructor(address: String) {
        val prefixSize = if (BuildConfig.DEBUG) TESTNET_PREFIX.length else BITSHARES_PREFIX.length
        this.prefix = address.substring(0..prefixSize)

        val decoded = Base58.decode(address.substring(prefixSize))
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

    override fun toString(): String {
        val pubKey = pubKey.toBytes()
        val checksum = calculateChecksum(pubKey)
        val pubKeyChecksummed = Bytes.concat(pubKey, checksum)
        return this.prefix + Base58.encode(pubKeyChecksummed)
    }

    companion object {
        const val BITSHARES_PREFIX = "GPH"
        const val TESTNET_PREFIX = "TEST"
    }

}
