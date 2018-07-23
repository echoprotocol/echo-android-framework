package com.pixelplex.echolib.support

import com.pixelplex.bitcoinj.ECDSASignature
import com.pixelplex.bitcoinj.ECKey
import com.pixelplex.bitcoinj.Sha256Hash
import com.pixelplex.bitcoinj.bigIntegerToBytes
import com.pixelplex.echolib.exception.MalformedTransactionException
import com.pixelplex.echolib.model.Transaction

/**
 * Object used to transaction signing
 *
 * @author Daria Pechkovskaya
 */
object Signature {

    private const val HEADER_BYTES = 1
    private const val R_BYTES = 32
    private const val S_BYTES = 32
    private const val SIGN_DATA_BYTES = HEADER_BYTES + R_BYTES + S_BYTES

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
     * <p>
     *     This should then be called before any other serialization method.
     * </p>
     *
     * @return: A valid signature of the transaction.
     */
    @JvmStatic
    fun signTransaction(transaction: Transaction): ByteArray {
        checkPrivateKey(transaction)

        val nonNullPrivateKey = transaction.privateKey!!

        var isGrapheneCanonical = false
        var signData: ByteArray = byteArrayOf()

        while (!isGrapheneCanonical) {
            val serializedTransaction = transaction.toBytes()
            val hash = Sha256Hash.wrap(Sha256Hash.hash(serializedTransaction))
            val key = ECKey.fromPrivate(nonNullPrivateKey)
            val sign = key.sign(hash)
            val recId = getRecId(sign, hash, key)

            signData = createSignData(key, recId, sign)

            if (isSignatureNotValid(signData)) {
                ++transaction.blockData.relativeExpiration
            } else {
                isGrapheneCanonical = true
            }

        }

        return signData
    }

    private fun checkPrivateKey(transaction: Transaction) {
        if (transaction.privateKey == null)
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
