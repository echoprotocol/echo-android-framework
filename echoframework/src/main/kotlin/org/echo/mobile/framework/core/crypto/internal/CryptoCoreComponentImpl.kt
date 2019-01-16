package org.echo.mobile.framework.core.crypto.internal

import com.google.common.primitives.Bytes
import org.echo.mobile.bitcoinj.ECKey
import org.echo.mobile.bitcoinj.Sha256Hash
import org.echo.mobile.framework.core.crypto.CryptoCoreComponent
import org.echo.mobile.framework.core.crypto.EchorandKeyProvider
import org.echo.mobile.framework.core.crypto.internal.addsa.EdDSACryptoAdapter
import org.echo.mobile.framework.core.logger.internal.LoggerCoreComponent
import org.echo.mobile.framework.model.AuthorityType
import org.echo.mobile.framework.model.Transaction
import org.echo.mobile.framework.model.network.Network
import org.echo.mobile.framework.support.checkTrue
import org.echo.mobile.framework.support.crypto.Checksum
import org.echo.mobile.framework.support.crypto.Signature
import org.echo.mobile.framework.support.crypto.decryptAES
import org.echo.mobile.framework.support.crypto.encryptAES
import org.echo.mobile.framework.support.hexlify
import org.echo.mobile.framework.support.sha256hash
import org.echo.mobile.framework.support.sha512hash
import org.spongycastle.util.encoders.Hex
import java.math.BigInteger
import java.security.NoSuchAlgorithmException
import java.util.ArrayList
import java.util.Arrays

/**
 * Implementation of [CryptoCoreComponent]
 *
 * Provides default implementation of cryptography core component
 * based on bitcoinj ECKey realization
 *
 * @author Daria Pechkovskaya
 * @author Dmitriy Bushuev
 */
class CryptoCoreComponentImpl(network: Network, edDSACryptoAdapter: EdDSACryptoAdapter) :
    CryptoCoreComponent {

    private val seedProvider = RoleDependentSeedProvider()
    private val ecKeyConverter = ECKeyToAddressConverter(network.addressPrefix)

    private val echorandKeyProvider: EchorandKeyProvider by lazy {
        EchorandKeyProviderImpl(edDSACryptoAdapter)
    }

    override fun getAddress(
        userName: String,
        password: String,
        authorityType: AuthorityType
    ): String {
        return ecKeyConverter.convert(
            ECKey.fromPrivate(
                getPrivateKey(userName, password, authorityType)
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

    override fun getEchorandKey(userName: String, password: String): String {
        val seedString = echorandSeed(userName, password)
        val secretKeySeed = ECKey.fromPrivate(createPrivateKey(seedString)).getPrivKeyBytes()
        return echorandKeyProvider.provide(secretKeySeed)
    }

    override fun getRawEchorandKey(userName: String, password: String): ByteArray {
        val seedString = echorandSeed(userName, password)
        val secretKeySeed = ECKey.fromPrivate(createPrivateKey(seedString)).getPrivKeyBytes()
        return echorandKeyProvider.provideRaw(secretKeySeed)
    }

    private fun generateSeed(userName: String, password: String, authorityType: AuthorityType) =
        seedProvider.provide(userName, password, authorityType)

    private fun createPrivateKey(seed: String): ByteArray {
        val seedBytes = seed.toByteArray(Charsets.UTF_8)
        return Sha256Hash.hash(seedBytes)
    }

    override fun signTransaction(transaction: Transaction): ArrayList<ByteArray> =
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
            val sharedSecret = secret.sha512hash()
            val seed = Bytes.concat(nonceBytes, Hex.toHexString(sharedSecret).hexlify())

            // Calculating checksum
            val sha256Msg = message.toByteArray().sha256hash()
            val checksum = Arrays.copyOfRange(sha256Msg, 0, Checksum.CHECKSUM_SIZE)

            // Concatenating checksum + message bytes
            val finalMessage = Bytes.concat(checksum, message.toByteArray())

            // Encryption
            encrypted = encryptAES(finalMessage, seed)
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
            val sharedSecret = secret.sha512hash()
            val seed = Bytes.concat(nonceBytes, Hex.toHexString(sharedSecret).hexlify())

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

    private fun echorandSeed(userName: String, password: String) =
        userName + ECHORAND_KEY_SEED_PART + password

    companion object {
        private val LOGGER = LoggerCoreComponent.create(CryptoCoreComponentImpl::class.java.name)

        private const val ECHORAND_KEY_SEED_PART = "echorand"
    }

}
