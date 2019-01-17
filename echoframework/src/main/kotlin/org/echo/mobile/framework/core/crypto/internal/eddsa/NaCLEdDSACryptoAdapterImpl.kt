package org.echo.mobile.framework.core.crypto.internal.eddsa

import org.echo.mobile.nacl.NaCl

/**
 * [EdDSACryptoAdapter] implementation based on NaCl library port for kotlin
 *
 * @author Dmitriy Bushuev
 */
class NaCLEdDSACryptoAdapterImpl : EdDSACryptoAdapter {

    override fun keyPair(): Pair<ByteArray, ByteArray> = NaCl.Box.keyPair()

    override fun keyPair(seed: ByteArray): Pair<ByteArray, ByteArray> =
        NaCl.Box.keyPairFromSecretKey(seed)

}