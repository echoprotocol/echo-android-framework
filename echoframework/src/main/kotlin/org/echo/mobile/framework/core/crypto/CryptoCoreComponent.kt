package org.echo.mobile.framework.core.crypto

import org.echo.mobile.framework.model.Transaction
import java.util.ArrayList

/**
 * Encapsulates logic, associated with keys generation and encryption/decryption processes
 *
 * @author Dmitriy Bushuev
 */
interface CryptoCoreComponent {

    /**
     * Generate random EdDSA raw private key
     */
    fun getEdDSAPrivateKey(): ByteArray

    /**
     * Derives public key from raw private key bytes  by EdDSA algorithm
     */
    fun deriveEdDSAPublicKeyFromPrivate(privateKey: ByteArray): ByteArray

    /**
     * Transforms public key raw bytes to account address format by EdDSA algorithm
     */
    fun getEdDSAAddressFromPublicKey(publicKey: ByteArray): String

    /**
     * Generates transaction signatures
     */
    fun signTransaction(transaction: Transaction): ArrayList<ByteArray>

    /**
     * Encodes private key in raw string representation to wif format
     */
    fun encodeToWif(source: ByteArray): String

    /**
     * Decodes private key in wif format [source] to the raw bytes representation
     */
    fun decodeFromWif(source: String): ByteArray

}
