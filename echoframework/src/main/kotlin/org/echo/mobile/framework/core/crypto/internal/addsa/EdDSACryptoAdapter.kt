package org.echo.mobile.framework.core.crypto.internal.addsa

/**
 * Adapter interface for EdDSA key pair generation
 *
 * Required for eddsa key generation API unification
 *
 * @author Dmitriy Bushuev
 */
interface EdDSACryptoAdapter {

    /**
     * Generates EdDSA key pair from random seed (secretKey)
     */
    fun keyPair(): Pair<ByteArray, ByteArray>

    /**
     * Generates EdDSA key pair using predefined [seed] as secret key
     *
     * [seed] size = 32 bytes !
     */
    fun keyPair(seed: ByteArray): Pair<ByteArray, ByteArray>

}