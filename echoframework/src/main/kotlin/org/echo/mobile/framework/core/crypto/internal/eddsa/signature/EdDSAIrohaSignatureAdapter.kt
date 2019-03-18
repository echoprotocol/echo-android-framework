package org.echo.mobile.framework.core.crypto.internal.eddsa.signature

import org.echo.mobile.framework.core.crypto.internal.eddsa.key.KeyPairGenerator
import org.echo.mobile.framework.exception.LocalException
import java.security.Signature

/**
 * [SignatureAdapter] implementation based on eddsa iroha library
 *
 * @author Dmitriy Bushuev
 */
class EdDSAIrohaSignatureAdapter : SignatureAdapter {

    override fun sign(source: ByteArray, privateKey: ByteArray): ByteArray =
        try {
            val private = privateKey.initPrivateKey()

            val signature = initSignature().apply {
                initSign(private)
                update(source)
            }

            signature.sign()
        } catch (exception: Exception) {
            throw LocalException("Error during signature process", exception)
        }

    private fun initSignature() =
        Signature.getInstance(
            SIGNATURE_ALGORITHM,
            SIGNATURE_PROVIDER
        )

    private fun ByteArray.initPrivateKey() =
        KeyPairGenerator()
            .apply { this.seed = this@initPrivateKey }
            .generateKeyPair()
            .private

    companion object {
        private const val SIGNATURE_ALGORITHM = "NONEwithEdDSA"
        private const val SIGNATURE_PROVIDER = "EdDSA"
    }

}