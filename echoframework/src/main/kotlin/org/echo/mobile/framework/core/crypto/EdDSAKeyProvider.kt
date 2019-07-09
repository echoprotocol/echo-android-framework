package org.echo.mobile.framework.core.crypto
/**
 * Describes functionality of echorand key generation for account registration
 *
 * @author Dmitriy Bushuev
 */
interface EdDSAKeyProvider {

    /**
     * Generates key in base58 representation using input [seed]
     *
     * [seed] size = 32 bytes !
     */
    fun provideAddress(seed: ByteArray): String

    /**
     * Generates key in base58 representation using random seed
     */
    fun provideAddress(): String

    /**
     * Generates key in raw representation
     */
    fun providePublicKeyRaw(seed: ByteArray): ByteArray

    /**
     * Generates private key in raw representation
     */
    fun providePrivateKeyRaw(seed: ByteArray? = null): ByteArray

    /**
     * Generates address from public key [ByteArray]
     */
    fun provideAddressFromPublicKey(key: ByteArray): String

}