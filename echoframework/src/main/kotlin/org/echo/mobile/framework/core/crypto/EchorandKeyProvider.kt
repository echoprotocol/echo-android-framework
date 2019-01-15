package org.echo.mobile.framework.core.crypto

/**
 * Describes functionality of echorand key generation for account registration
 *
 * @author Dmitriy Bushuev
 */
interface EchorandKeyProvider {

    /**
     * Generates echorand key in base58 representation using input [seed]
     *
     * [seed] size = 32 bytes !
     */
    fun provide(seed: ByteArray): String

    /**
     * Generates echorand key in base58 representation using random seed
     */
    fun provide(): String

}