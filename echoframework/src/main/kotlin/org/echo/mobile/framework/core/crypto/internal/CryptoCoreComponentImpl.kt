package org.echo.mobile.framework.core.crypto.internal

import org.echo.mobile.framework.core.crypto.CryptoCoreComponent
import org.echo.mobile.framework.core.crypto.EdDSAKeyProvider
import org.echo.mobile.framework.core.crypto.WifProcessor
import org.echo.mobile.framework.core.crypto.internal.eddsa.key.KeyPairCryptoAdapter
import org.echo.mobile.framework.core.logger.internal.LoggerCoreComponent
import org.echo.mobile.framework.exception.LocalException
import org.echo.mobile.framework.model.Transaction
import org.echo.mobile.framework.support.crypto.EdDSASignature
import java.util.ArrayList

/**
 * Implementation of [CryptoCoreComponent]
 *
 * Provides default implementation of cryptography core component
 * based on bitcoinj ECKey realization
 *
 * @author Daria Pechkovskaya
 * @author Dmitriy Bushuev
 */
class CryptoCoreComponentImpl @JvmOverloads constructor(
    keyPairCryptoAdapter: KeyPairCryptoAdapter,
    private val wifProcessor: WifProcessor = DefaultWifProcessor(true)
) :
    CryptoCoreComponent {

    private val edDSAKeyProvider: EdDSAKeyProvider by lazy {
        EdDSAKeyProviderImpl(keyPairCryptoAdapter)
    }

    override fun getEdDSAPrivateKey(): ByteArray {
        return edDSAKeyProvider.providePrivateKeyRaw()
    }

    override fun deriveEdDSAPublicKeyFromPrivate(privateKey: ByteArray): ByteArray =
        try {
            edDSAKeyProvider.providePublicKeyRaw(privateKey)
        } catch (exception: Exception) {
            throw LocalException("Public key derivation error", exception)
        }

    override fun getEdDSAAddressFromPublicKey(publicKey: ByteArray): String =
        try {
            edDSAKeyProvider.provideAddressFromPublicKey(publicKey)
        } catch (exception: Exception) {
            throw LocalException("Address from public key derivation error", exception)
        }

    override fun signTransaction(transaction: Transaction): ArrayList<ByteArray> =
        EdDSASignature.signTransaction(transaction)

    override fun encodeToWif(source: ByteArray): String = wifProcessor.encodeToWif(source)

    override fun decodeFromWif(source: String): ByteArray = wifProcessor.decodeFromWif(source)

    companion object {
        private val LOGGER = LoggerCoreComponent.create(CryptoCoreComponentImpl::class.java.name)
    }

}
