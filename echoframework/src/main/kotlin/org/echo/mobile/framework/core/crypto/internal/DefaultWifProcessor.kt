package org.echo.mobile.framework.core.crypto.internal

import org.echo.mobile.bitcoinj.Base58
import org.echo.mobile.bitcoinj.Sha256Hash
import org.echo.mobile.framework.core.crypto.WifProcessor
import org.echo.mobile.framework.exception.LocalException
import org.spongycastle.util.encoders.Hex

/**
 * Default [WifProcessor] implementation based on standard WIF format
 *
 * @author Dmitriy Bushuev
 */
class DefaultWifProcessor(private val isMainNet: Boolean) : WifProcessor {

    override fun encodeToWif(source: ByteArray): String = generateWifKey(source, isMainNet)

    override fun decodeFromWif(source: String): ByteArray = fromWif(source, isMainNet)

    private fun generateWifKey(privateKey: ByteArray, isMainNet: Boolean): String {
        try {
            val bytes = Hex.decode(getFrontBytes(isMainNet)) + privateKey
            val firstSha256 = Sha256Hash.hash(bytes)
            val secondSha256 = Sha256Hash.hash(firstSha256)
            val checksum = secondSha256.copyOfRange(
                0,
                CHECKSUM_SIZE
            )
            val str = bytes + checksum

            return Base58.encode(str)
        } catch (exception: Exception) {
            throw LocalException("WIF encoding error", exception)
        }
    }

    private fun fromWif(source: String, isMainNet: Boolean): ByteArray {
        try {
            val decodedWif = Base58.decode(source)

            val checksum = decodedWif.slice(decodedWif.size - CHECKSUM_SIZE until decodedWif.size)
            val privateKey = decodedWif.slice(0 until decodedWif.size - CHECKSUM_SIZE)

            val requiredChecksum =
                Sha256Hash.hash(Sha256Hash.hash(privateKey.toByteArray())).copyOfRange(
                    0,
                    CHECKSUM_SIZE
                )

            if (!requiredChecksum.contentEquals(checksum.toByteArray())) {
                throw LocalException(
                    "Wrong checksum error. Checksum should be the same. Required ${Hex.encode(
                        requiredChecksum
                    )}, received ${Hex.encode(checksum.toByteArray())}"
                )
            }

            val rawPrivate = privateKey.toByteArray()

            val networkBytes = rawPrivate[0]
            val requiredNetworkBytes = Hex.decode(getFrontBytes(isMainNet))

            if (!requiredNetworkBytes!!.contentEquals(byteArrayOf(networkBytes))) {
                throw LocalException(
                    "Wrong wif format. Network bytes should be the same. Required ${Hex.encode(
                        requiredNetworkBytes
                    )}, received ${Hex.encode(byteArrayOf(networkBytes))}"
                )
            }

            val decodedPrivate = rawPrivate.slice(1 until rawPrivate.size)

            return decodedPrivate.toByteArray()
        } catch (exception: Exception) {
            throw LocalException("WIF decoding error", exception)
        }
    }

    private fun getFrontBytes(isMainNet: Boolean): String =
        if (isMainNet) MAIN_NET_FRONT_BYTES else TEST_NET_FRONT_BYTES

    companion object {
        const val CHECKSUM_SIZE = 4

        const val MAIN_NET_FRONT_BYTES = "80"
        const val TEST_NET_FRONT_BYTES = "ef"
    }

}