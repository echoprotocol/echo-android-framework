package com.pixelplex.echoframework.model

import com.google.common.primitives.Bytes
import com.pixelplex.bitcoinj.Base58
import com.pixelplex.echoframework.exception.MalformedAddressException
import com.pixelplex.echoframework.model.network.Network
import com.pixelplex.echoframework.support.Checksum.CHECKSUM_SIZE
import com.pixelplex.echoframework.support.Checksum.calculateChecksum

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

    constructor(pubKey: PublicKey) {
        this.pubKey = pubKey
        this.prefix = pubKey.network.addressPrefix
    }

    constructor(address: String, network: Network) {
        val prefixSize = network.addressPrefix.length
        this.prefix = address.substring(0..prefixSize)

        val decoded = Base58.decode(address.substring(prefixSize))
        val pubKey = decoded.copyOfRange(0, decoded.size - CHECKSUM_SIZE)
        this.pubKey = PublicKey(pubKey)

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
