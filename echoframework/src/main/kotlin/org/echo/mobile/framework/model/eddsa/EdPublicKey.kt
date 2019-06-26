package org.echo.mobile.framework.model.eddsa

import org.echo.mobile.framework.model.ByteSerializable
import java.io.Serializable

/**
 * Encapsulates logic of working with account key/address by EdDSA algorithm
 *
 * @author Dmitriy Bushuev
 */
class EdPublicKey @JvmOverloads constructor(
    key: ByteArray,
    var addressPrefix: String = ""
) : Serializable, ByteSerializable {

    var key: ByteArray = key
        private set

    val address: String
        get() {
            val publicKey =
                EdPublicKey(key, addressPrefix)
            return EdAddress(publicKey).toString()
        }

    override fun toBytes(): ByteArray = key

    override fun hashCode(): Int = key.hashCode()

    override fun equals(other: Any?): Boolean = other is EdPublicKey && key.contentEquals(other.key)

}
