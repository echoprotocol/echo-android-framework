package org.echo.mobile.framework.core.crypto.internal

import org.echo.mobile.bitcoinj.Base58
import org.echo.mobile.bitcoinj.ECKey
import org.echo.mobile.framework.support.crypto.Checksum
import org.echo.mobile.framework.support.Converter

/**
 * Converts ECKey to address representation
 * [org.echo.mobile.framework.model.Address]
 *
 * @author Dmitriy Bushuev
 */
class ECKeyToAddressConverter(private val prefix: String) : Converter<ECKey, String> {

    override fun convert(source: ECKey) = source.toAddress()

    private fun ECKey.toAddress(): String {
        val pubKey = getPubKey(this)
        val checksum = Checksum.calculateChecksum(pubKey)
        val pubKeyWithChecksum = pubKey + checksum
        return prefix + Base58.encode(pubKeyWithChecksum)
    }

    private fun getPubKey(key: ECKey): ByteArray =
        if (key.isCompressed) {
            key.pubKey
        } else {
            val compressedKey = ECKey.fromPublicOnly(ECKey.compressPoint(key.pubKeyPoint))
            compressedKey.pubKey
        }

}
