package org.echo.mobile.framework.core.crypto

import org.echo.mobile.bitcoinj.ECKey

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
    fun provide(seed: ByteArray): String

    /**
     * Generates key in base58 representation using random seed
     */
    fun provide(): String

    /**
     * Generates key in raw representation
     */
    fun provideRaw(seed: ByteArray): ByteArray

    /**
     * Generates key from public [ECKey]
     */
    fun provideFromPublic(key: ECKey): String

}