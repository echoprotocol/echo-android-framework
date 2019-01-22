package org.echo.mobile.framework.core.crypto.internal.eddsa.key

import org.echo.mobile.nacl.NaCl

/**
 * [KeyPairCryptoAdapter] implementation based on NaCl library port for kotlin
 *
 * @author Dmitriy Bushuev
 */
class NaCLKeyPairCryptoAdapter :
    KeyPairCryptoAdapter {

    override fun keyPair(): Pair<ByteArray, ByteArray> = NaCl.Box.keyPair()

    override fun keyPair(seed: ByteArray): Pair<ByteArray, ByteArray> =
        NaCl.Box.keyPairFromSecretKey(seed)

}