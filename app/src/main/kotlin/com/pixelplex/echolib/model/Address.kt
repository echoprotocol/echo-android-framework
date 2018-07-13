package com.pixelplex.echolib.model

import com.pixelplex.bitcoinj.Base58
import com.pixelplex.bitcoinj.ECKey
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

    companion object {
        const val BITSHARES_PREFIX = "GPH"
        const val PREFIX_SIZE = 3
    }

}
