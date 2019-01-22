package org.echo.mobile.framework.support.crypto

import org.echo.mobile.bitcoinj.ECDSASignature
import org.echo.mobile.bitcoinj.ECKey
import org.echo.mobile.bitcoinj.Sha256Hash
import org.echo.mobile.bitcoinj.bigIntegerToBytes
import org.echo.mobile.framework.exception.MalformedTransactionException
import org.echo.mobile.framework.model.Transaction
import java.util.ArrayList

/**
 * Object used to transaction signing
 *
 * @author Daria Pechkovskaya
 */
object Signature {

    private const val HEADER_BYTES = 1
    private const val R_BYTES = 32
    private const val S_BYTES = 32
    const val SIGN_DATA_BYTES = HEADER_BYTES + R_BYTES + S_BYTES

    private const val HEADER_POS = 0
    private const val R_BYTES_POS = HEADER_POS + HEADER_BYTES
    private const val S_BYTES_POS = R_BYTES_POS + R_BYTES

    private const val COMPACT_HEADER_SIZE = 27
    private const val COMPRESSION_SIZE = 4
    private const val CHECKING_BYTE = 0x80

    private const val MAX_REC_ID_INDEX = 3

    /**
     * Obtains a signature of transaction. Please note that due to the current reliance on
     * bitcoinj to generate the signatures, and due to the fact that it uses deterministic
     * ecdsa signatures, we are slightly modifying the expiration time of the transaction while
     * we look for a signature that will be accepted by the graphene network.
     *
     * This should then be called before any other serialization method.
     *
     * @return: A valid signature of the transaction.
     */
    @JvmStatic
    fun signTransaction(transaction: Transaction): ArrayList<ByteArray> {
        checkPrivateKeys(transaction)

        var isGrapheneCanonical = false
        val signatures = ArrayList<ByteArray>()

        var sigData: ByteArray

        while (!isGrapheneCanonical) {
            signatures.clear()
            val serializedTransaction = transaction.toBytes()
            val hash = Sha256Hash.wrap(Sha256Hash.hash(serializedTransaction))
            for (privateKey in transaction.privateKeys) {
                val key = ECKey.fromPrivate(privateKey)
                val sign = key.sign(hash)
                val recId = getRecId(sign, hash, key)

                sigData = createSignData(
                    key,
                    recId,
                    sign
                )

                if (isSignatureNotValid(sigData)) {
                    break
                } else {
                    signatures.add(sigData)
                }
            }

            if (signatures.size == transaction.privateKeys.size) {
                isGrapheneCanonical = true
            } else {
                ++transaction.blockData.relativeExpiration
            }
        }
        return signatures
    }

    private fun checkPrivateKeys(transaction: Transaction) {
        if (transaction.privateKeys.isEmpty())
            throw MalformedTransactionException("Transaction must have private key for signing.")
    }

    private fun getRecId(sign: ECDSASignature, hash: Sha256Hash, privateKey: ECKey): Int {
        for (i in 0..MAX_REC_ID_INDEX) {
            val key = ECKey.recoverFromSignature(i, sign, hash, privateKey.isCompressed)
            if (key != null && key.pubKeyPoint == privateKey.pubKeyPoint) {
                return i
            }
        }
        return -1
    }

    private fun createSignData(
        privateKey: ECKey,
        recId: Int,
        sign: ECDSASignature
    ): ByteArray {
        val signData = ByteArray(SIGN_DATA_BYTES)
        val compressionBytes = if (privateKey.isCompressed) COMPRESSION_SIZE else 0
        val headerByte = (recId + COMPACT_HEADER_SIZE + compressionBytes).toByte()
        signData[HEADER_POS] = headerByte
        System.arraycopy(
            sign.r.bigIntegerToBytes(R_BYTES),
            0,
            signData,
            R_BYTES_POS,
            R_BYTES
        )
        System.arraycopy(
            sign.s.bigIntegerToBytes(S_BYTES),
            0,
            signData,
            S_BYTES_POS,
            S_BYTES
        )
        return signData
    }

    private fun isSignatureNotValid(signData: ByteArray): Boolean =
        signData[HEADER_POS].toInt() and CHECKING_BYTE != 0
                || signData[HEADER_POS].toInt() == 0
                || signData[R_BYTES_POS].toInt() and CHECKING_BYTE != 0
                || signData[R_BYTES].toInt() and CHECKING_BYTE != 0
                || signData[S_BYTES].toInt() == 0
                || signData[S_BYTES_POS].toInt() and CHECKING_BYTE != 0

}
