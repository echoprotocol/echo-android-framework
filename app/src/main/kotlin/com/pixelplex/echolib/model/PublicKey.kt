package com.pixelplex.echolib.model

import org.bitcoinj.core.ECKey
import java.io.Serializable


/**
 * Encapsulates logic of working with account key/address
 *
 * @author Dmitriy Bushuev
 */
class PublicKey(key: ECKey) : Serializable, ByteSerializable {

    var key: ECKey = key
        private set

    val address: String
        get() {
            var pubKey = ECKey.fromPublicOnly(key.pubKey)
            pubKey = getCompressedKey(pubKey)
            val publicKey = PublicKey(pubKey)
            return Address(publicKey).toString()
        }

    init {
        if (key.hasPrivKey()) {
            throw IllegalStateException("Passing a private key to PublicKey constructor")
        }
    }

    override fun toBytes(): ByteArray =
        getCompressedKey(key).pubKey

    private fun getCompressedKey(key: ECKey): ECKey {
        if (key.isCompressed) return key

        val compressPoint = ECKey.compressPoint(key.pubKeyPoint)
        return ECKey.fromPublicOnly(compressPoint)
    }

    override fun hashCode(): Int = key.hashCode()

    override fun equals(other: Any?): Boolean = other is PublicKey && key == other.key

}
