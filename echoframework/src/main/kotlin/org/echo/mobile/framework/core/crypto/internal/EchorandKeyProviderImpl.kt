package org.echo.mobile.framework.core.crypto.internal

import org.echo.mobile.bitcoinj.Base58
import org.echo.mobile.framework.core.crypto.EchorandKeyProvider
import org.echo.mobile.framework.core.crypto.internal.EchorandKeyProviderImpl.Companion.ECHORAND_KEY_PREFIX
import org.echo.mobile.framework.core.crypto.internal.addsa.EdDSACryptoAdapter
import org.echo.mobile.framework.exception.LocalException

/**
 * [EchorandKeyProvider] implementation based on realisations of [EdDSACryptoAdapter]
 *
 * Returns public key of generated key pair encoded in base58. Added specified [prefix].
 * Default prefix - [ECHORAND_KEY_PREFIX]
 *
 * Wraps all errors in [LocalException]
 *
 * @author Dmitriy Bushuev
 */
class EchorandKeyProviderImpl(
    private val edDSACryptoAdapter: EdDSACryptoAdapter,
    private val prefix: String = ECHORAND_KEY_PREFIX
) :
    EchorandKeyProvider {

    override fun provide(seed: ByteArray): String =
        wrapError { prefix + Base58.encode(edDSACryptoAdapter.keyPair(seed).first) }

    override fun provide(): String =
        wrapError { prefix + Base58.encode(edDSACryptoAdapter.keyPair().first) }

    override fun provideRaw(seed: ByteArray): ByteArray =
        wrapError { edDSACryptoAdapter.keyPair(seed).first }

    private fun <T> wrapError(body: () -> T): T =
        try {
            body()
        } catch (exception: Exception) {
            throw LocalException("Error occurred during eddsa key generation", exception)
        }

    companion object {
        private const val ECHORAND_KEY_PREFIX = "DET"
    }

}