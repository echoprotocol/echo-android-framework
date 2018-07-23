package com.pixelplex.echolib.model

import com.pixelplex.echolib.model.network.Network
import com.pixelplex.echolib.model.network.Testnet
import java.io.Serializable

/**
 * Encapsulates logic of working with account key/address
 *
 * @author Dmitriy Bushuev
 */
class PublicKey @JvmOverloads constructor(
    key: ByteArray,
    var network: Network = Testnet()
) : Serializable, ByteSerializable {

    var key: ByteArray = key
        private set

    val address: String
        get() {
            val publicKey = PublicKey(key, network)
            return Address(publicKey).toString()
        }

    override fun toBytes(): ByteArray = key

    override fun hashCode(): Int = key.hashCode()

    override fun equals(other: Any?): Boolean = other is PublicKey && key.contentEquals(other.key)

}
