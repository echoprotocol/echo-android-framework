package com.pixelplex.echolib.model

import com.pixelplex.bitcoinj.ECKey
import com.pixelplex.echolib.model.network.Network
import com.pixelplex.echolib.model.network.Testnet
import com.pixelplex.echolib.support.checkFalse
import java.io.Serializable

/**
 * Encapsulates logic of working with account key/address
 *
 * @author Dmitriy Bushuev
 */
class PublicKey @JvmOverloads constructor(
    key: ECKey,
    var network: Network = Testnet()
) : Serializable, ByteSerializable {

    var key: ECKey = key
        private set

    val address: String
        get() {
            var pubKey = ECKey.fromPublicOnly(key.pubKey)
            pubKey = getCompressedKey(pubKey)
            val publicKey = PublicKey(pubKey, network)
            return Address(publicKey).toString()
        }

    init {
        checkFalse(key.hasPrivKey(), "Passing a private key to PublicKey constructor")
    }

    private fun getCompressedKey(key: ECKey): ECKey {
        if (key.isCompressed) return key

        val compressPoint = ECKey.compressPoint(key.pubKeyPoint)
        return ECKey.fromPublicOnly(compressPoint)
    }

    override fun toBytes(): ByteArray = getCompressedKey(key).pubKey

    override fun hashCode(): Int = key.hashCode()

    override fun equals(other: Any?): Boolean = other is PublicKey && key == other.key

}
