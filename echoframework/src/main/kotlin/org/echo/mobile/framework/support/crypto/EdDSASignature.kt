package org.echo.mobile.framework.support.crypto

import org.echo.mobile.bitcoinj.Sha256Hash
import org.echo.mobile.framework.core.crypto.internal.eddsa.signature.EdDSAIrohaSignatureAdapter
import org.echo.mobile.framework.exception.MalformedTransactionException
import org.echo.mobile.framework.model.Transaction
import java.util.ArrayList

/**
 * Object used to transaction EdDSA signing
 *
 * @author Daria Pechkovskaya
 */
object EdDSASignature {

    private const val HEADER_BYTES = 1
    private const val R_BYTES = 32
    private const val S_BYTES = 32
    const val SIGN_DATA_BYTES = R_BYTES + S_BYTES

    private const val HEADER_POS = 0
    private const val R_BYTES_POS = HEADER_POS + HEADER_BYTES
    private const val S_BYTES_POS = R_BYTES_POS + R_BYTES

    private const val CHECKING_BYTE = 0x80

    /**
     * Obtains a signature of transaction. Please note that due to the fact that it uses
     * deterministic EdDSA signatures, we are slightly modifying the expiration time of
     * the transaction while we look for a signature that will be accepted by
     * the graphene network.
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

        val signatureAdapter = EdDSAIrohaSignatureAdapter()

        var sigData: ByteArray

        while (!isGrapheneCanonical) {
            signatures.clear()
            val serializedTransaction = transaction.toBytes()
            val hash = Sha256Hash.wrap(Sha256Hash.hash(serializedTransaction))
            for (privateKey in transaction.privateKeys) {
                sigData = signatureAdapter.sign(hash.bytes, privateKey)

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

    private fun isSignatureNotValid(signData: ByteArray): Boolean =
        signData[HEADER_POS].toInt() and CHECKING_BYTE != 0
                || signData[HEADER_POS].toInt() == 0
                || signData[R_BYTES_POS].toInt() and CHECKING_BYTE != 0
                || signData[R_BYTES].toInt() and CHECKING_BYTE != 0
                || signData[S_BYTES].toInt() == 0
                || signData[S_BYTES_POS].toInt() and CHECKING_BYTE != 0

}
