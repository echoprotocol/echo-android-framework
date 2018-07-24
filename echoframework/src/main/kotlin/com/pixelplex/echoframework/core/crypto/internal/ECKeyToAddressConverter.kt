package com.pixelplex.echoframework.core.crypto.internal

import com.pixelplex.bitcoinj.Base58
import com.pixelplex.bitcoinj.ECKey
import com.pixelplex.echoframework.support.Checksum
import com.pixelplex.echoframework.support.Converter

/**
 * Converts ECKey to address representation
 * (@see com.pixelplex.echolib.model.Address)
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
