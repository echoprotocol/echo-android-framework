package org.echo.mobile.framework.core.crypto.internal.eddsa.key

import jp.co.soramitsu.crypto.ed25519.EdDSAPrivateKey
import jp.co.soramitsu.crypto.ed25519.EdDSAPublicKey

/**
 * [KeyPairCryptoAdapter] implementation based on iroha library port for java
 *
 * @author Dmitriy Bushuev
 */
class IrohaKeyPairCryptoAdapter : KeyPairCryptoAdapter {

    override fun keyPair(): Pair<ByteArray, ByteArray> = KeyPairGenerator().generate()

    override fun keyPair(seed: ByteArray): Pair<ByteArray, ByteArray> =
        KeyPairGenerator().apply { this.seed = seed }.generate()

    private fun KeyPairGenerator.generate(): Pair<ByteArray, ByteArray> {
        val keyPair = this.generateKeyPair()

        val public = (keyPair.public as EdDSAPublicKey).abyte
        val private = (keyPair.private as EdDSAPrivateKey).seed

        return Pair(public, private)
    }

}