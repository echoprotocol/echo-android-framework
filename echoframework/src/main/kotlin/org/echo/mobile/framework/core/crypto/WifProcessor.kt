package org.echo.mobile.framework.core.crypto

/**
 * Provides methods for working with private key WIF format
 *
 * @author Dmitriy Bushuev
 */
interface WifProcessor {

    /**
     * Encodes private key in raw string representation to wif format
     */
    fun encodeToWif(source: ByteArray): String

    /**
     * Decodes private key in wif format [source] to the raw bytes representation
     */
    fun decodeFromWif(source: String): ByteArray

}