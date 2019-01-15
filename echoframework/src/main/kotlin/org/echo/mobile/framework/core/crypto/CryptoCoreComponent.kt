package org.echo.mobile.framework.core.crypto

import org.echo.mobile.framework.model.AuthorityType
import org.echo.mobile.framework.model.Transaction
import java.math.BigInteger
import java.util.ArrayList

/**
 * Encapsulates logic, associated with keys generation and encryption/decryption processes
 *
 * @author Dmitriy Bushuev
 */
interface CryptoCoreComponent {

    /**
     * Generates unique address string from username and password
     */
    fun getAddress(userName: String, password: String, authorityType: AuthorityType): String

    /**
     * Generates private elliptic curve key for transaction signing
     */
    fun getPrivateKey(userName: String, password: String, authorityType: AuthorityType): ByteArray

    /**
     * Generates echorand key using [userName] and password
     */
    fun getEchorandKey(userName: String, password: String): String

    /**
     * Generates transaction signatures
     */
    fun signTransaction(transaction: Transaction): ArrayList<ByteArray>

    /**
     * Encrypts string message using input keys
     *
     * @param privateKey Private key of one of the transfer operation parties
     * @param publicKey  Public key of another participant of transfer
     * @param nonce      Random entropy of memo payload for deriving unique key for every transfer
     * @param message    Message for encrypting
     */
    fun encryptMessage(
        privateKey: ByteArray, publicKey: ByteArray, nonce: BigInteger,
        message: String
    ): ByteArray?

    /**
     * Decrypts string message using input keys
     *
     * @param privateKey Private key of one of the transfer operation parties
     * @param publicKey  Public key of another participant of transfer
     * @param nonce      Random entropy of memo payload for deriving unique key for every transfer
     * @param message    Message for decrypting
     */
    fun decryptMessage(
        privateKey: ByteArray, publicKey: ByteArray, nonce: BigInteger,
        message: ByteArray
    ): String

}
