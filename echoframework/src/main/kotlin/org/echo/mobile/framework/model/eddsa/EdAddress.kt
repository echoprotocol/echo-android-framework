package org.echo.mobile.framework.model.eddsa

import org.echo.mobile.bitcoinj.Base58

/**
 * Represents EdDSA address model in blockchain
 *
 * @author Daria Pechkovskaya
 */
class EdAddress {

    var pubKey: EdPublicKey
        private set

    private val prefix: String

    constructor(pubKey: EdPublicKey) {
        this.pubKey = pubKey
        this.prefix = pubKey.addressPrefix
    }

    constructor(address: String, addressPrefix: String = DET_PREFIX) {
        val prefixSize = addressPrefix.length
        this.prefix = address.substring(0 until prefixSize)

        val pubKey = Base58.decode(address.substring(prefixSize))
        this.pubKey = EdPublicKey(pubKey, prefix)
    }

    override fun toString(): String = prefix + Base58.encode(pubKey.toBytes())

    companion object {
        const val DET_PREFIX = "ECHO"
    }

}
