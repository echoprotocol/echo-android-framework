package org.echo.mobile.framework.model

import org.echo.mobile.framework.model.network.Echodevnet
import org.echo.mobile.framework.model.network.Network
import java.io.Serializable

/**
 * Encapsulates logic of working with account key/address
 *
 * @author Dmitriy Bushuev
 */
class PublicKey @JvmOverloads constructor(
    key: ByteArray,
    var network: Network = Echodevnet()
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
