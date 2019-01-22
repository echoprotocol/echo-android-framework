package org.echo.mobile.framework.core.crypto.internal.eddsa.signature

/**
 * Adapter interface for signing
 *
 * Required for signing API unification
 *
 * @author Dmitriy Bushuev
 */
interface SignatureAdapter {

    /**
     * Signs [source] bytes using [privateKey]
     */
    fun sign(source: ByteArray, privateKey: ByteArray): ByteArray

}