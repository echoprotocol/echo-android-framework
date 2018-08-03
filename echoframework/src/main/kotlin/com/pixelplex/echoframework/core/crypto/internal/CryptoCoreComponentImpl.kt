package com.pixelplex.echoframework.core.crypto.internal

import com.google.common.primitives.Bytes
import com.pixelplex.bitcoinj.ECKey
import com.pixelplex.bitcoinj.Sha256Hash
import com.pixelplex.echoframework.core.crypto.CryptoCoreComponent
import com.pixelplex.echoframework.core.logger.internal.LoggerCoreComponent
import com.pixelplex.echoframework.model.AuthorityType
import com.pixelplex.echoframework.model.Transaction
import com.pixelplex.echoframework.model.network.Network
import com.pixelplex.echoframework.support.*
import org.spongycastle.util.encoders.Hex
import java.math.BigInteger
import java.security.NoSuchAlgorithmException
import java.util.*

/**
 * Implementation of [CryptoCoreComponent]
 *
 * Provides default implementation of cryptography core component
 * based on bitcoinj ECKey realization
 *
 * @author Daria Pechkovskaya
 * @author Dmitriy Bushuev
 */
class CryptoCoreComponentImpl(network: Network) : CryptoCoreComponent {

    private val seedProvider = RoleDependentSeedProvider()
    private val ecKeyConverter = ECKeyToAddressConverter(network.addressPrefix)

    override fun getAddress(
        userName: String,
        password: String,
        authorityType: AuthorityType
    ): String {
        return ecKeyConverter.convert(
            ECKey.fromPrivate(
                getPrivateKey(
                    userName,
                    password,
                    authorityType
                )
            )
        )
    }

    override fun getPrivateKey(
        userName: String,
        password: String,
        authorityType: AuthorityType
    ): ByteArray {
        val seedString = generateSeed(userName, password, authorityType)
        return ECKey.fromPrivate(createPrivateKey(seedString)).getPrivKeyBytes()
    }

    private fun generateSeed(userName: String, password: String, authorityType: AuthorityType) =
        seedProvider.provide(userName, password, authorityType)

    private fun createPrivateKey(seed: String): ByteArray {
        val seedBytes = seed.toByteArray(Charsets.UTF_8)
        return Sha256Hash.hash(seedBytes)
    }

    override fun signTransaction(transaction: Transaction): ByteArray =
        Signature.signTransaction(transaction)

    override fun encryptMessage(
        privateKey: ByteArray,
        publicKey: ByteArray,
        nonce: BigInteger,
        message: String
    ): ByteArray? {
        var encrypted: ByteArray? = null
        try {
            // Getting nonce bytes
            val stringNonce = nonce.toString()
            val nonceBytes =
                Arrays.copyOfRange(stringNonce.hexlify(), 0, stringNonce.length)

            // Getting shared secret
            val ecPublicKey = ECKey.fromPublicOnly(publicKey)
            val ecPrivateKey = ECKey.fromPrivate(privateKey)
            val secret =
                ecPublicKey.pubKeyPoint.multiply(ecPrivateKey.priv).normalize().xCoord.encoded

            // SHA-512 of shared secret
            val ss = secret.sha512hash()
            val seed = Bytes.concat(nonceBytes, Hex.toHexString(ss).hexlify())

            // Calculating checksum
            val sha256Msg = message.toByteArray().sha256hash()
            val checksum = Arrays.copyOfRange(sha256Msg, 0, Checksum.CHECKSUM_SIZE)

            // Concatenating checksum + message bytes
            val msgFinal = Bytes.concat(checksum, message.toByteArray())

            // Encryption
            encrypted = encryptAES(msgFinal, seed)
        } catch (e: NoSuchAlgorithmException) {
            LOGGER.log("Error occurred during creating digest for nonexistent algorithm", e)
        } catch (e: Exception) {
            LOGGER.log("Error occurred during message encryption", e)
        }

        return encrypted
    }

    override fun decryptMessage(
        privateKey: ByteArray,
        publicKey: ByteArray,
        nonce: BigInteger,
        message: ByteArray
    ): String {
        var plaintext = ""
        try {
            // Getting nonce bytes
            val stringNonce = nonce.toString()
            val nonceBytes = Arrays.copyOfRange(stringNonce.hexlify(), 0, stringNonce.length)

            // Getting shared secret
            val ecPublicKey = ECKey.fromPublicOnly(publicKey)
            val ecPrivateKey = ECKey.fromPrivate(privateKey)
            val secret =
                ecPublicKey.pubKeyPoint.multiply(ecPrivateKey.priv).normalize().xCoord.encoded

            // Secret hash
            val ss = secret.sha512hash()
            val seed = Bytes.concat(nonceBytes, Hex.toHexString(ss).hexlify())

            // Decryption
            val temp = decryptAES(message, seed)
            val decrypted = Arrays.copyOfRange(temp, Checksum.CHECKSUM_SIZE, temp?.size ?: 0)
            plaintext = String(decrypted)

            // checksum verification!
            val checksum = Arrays.copyOfRange(temp, 0, Checksum.CHECKSUM_SIZE)
            val verificationChecksum =
                Arrays.copyOfRange(plaintext.toByteArray().sha256hash(), 0, Checksum.CHECKSUM_SIZE)

            checkTrue(
                Arrays.equals(checksum, verificationChecksum),
                "Corrupted message. Checksum should be the same."
            )
        } catch (e: NoSuchAlgorithmException) {
            LOGGER.log("Error occurred during creating digest for nonexistent algorithm", e)
        } catch (e: Exception) {
            LOGGER.log("Error occurred during message decryption", e)
        }

        return plaintext
    }

    companion object {
        private val LOGGER = LoggerCoreComponent.create(CryptoCoreComponentImpl::class.java.name)
    }

}
