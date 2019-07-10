package org.echo.mobile.framework.model

import com.google.common.primitives.Bytes
import org.echo.mobile.bitcoinj.Base58
import org.echo.mobile.framework.model.network.Network
import org.echo.mobile.framework.support.crypto.Checksum.calculateChecksum

/**
 * Represents EcDSA address model in blockchain
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
        this.prefix = address.substring(0 until prefixSize)

        val decoded = Base58.decode(address.substring(prefixSize))
        val pubKey = decoded.copyOfRange(0, decoded.size)
        this.pubKey = PublicKey(pubKey, network)
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
        const val DEVNET_PREFIX = "ECHO"
    }

}
