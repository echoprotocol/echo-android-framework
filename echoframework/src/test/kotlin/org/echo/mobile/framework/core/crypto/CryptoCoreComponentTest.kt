package org.echo.mobile.framework.core.crypto

import com.google.common.primitives.UnsignedLong
import org.echo.mobile.bitcoinj.ECKey
import org.echo.mobile.framework.core.crypto.internal.CryptoCoreComponentImpl
import org.echo.mobile.framework.core.crypto.internal.eddsa.key.NaCLKeyPairCryptoAdapter
import org.echo.mobile.framework.model.Account
import org.echo.mobile.framework.model.Address
import org.echo.mobile.framework.model.Asset
import org.echo.mobile.framework.model.AssetAmount
import org.echo.mobile.framework.model.AuthorityType
import org.echo.mobile.framework.model.BlockData
import org.echo.mobile.framework.model.Transaction
import org.echo.mobile.framework.model.network.Echodevnet
import org.echo.mobile.framework.model.network.Network
import org.echo.mobile.framework.model.operations.TransferOperation
import org.echo.mobile.framework.model.operations.TransferOperationBuilder
import org.echo.mobile.framework.support.crypto.Signature.SIGN_DATA_BYTES
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigInteger

/**
 * Test cases for [CryptoCoreComponentImpl]
 *
 * @author Dmitriy Bushuev
 */
class CryptoCoreComponentTest {

    private lateinit var cryptoCoreComponent: CryptoCoreComponent

    private val name = "testName"
    private val password = "testPassword"

    private val secondName = "secondTestName"
    private val secondPassword = "secondTestPassword"

    private lateinit var network: Network
    private val authorityType = AuthorityType.ACTIVE

    private val message = "testMessage"

    @Before
    fun setUp() {
        network = Echodevnet()
        cryptoCoreComponent = CryptoCoreComponentImpl(network,
            NaCLKeyPairCryptoAdapter()
        )
    }

    @Test
    fun addressEqualityTest() {
        val firstAddress = cryptoCoreComponent.getAddress(name, password, authorityType)
        val secondAddress = cryptoCoreComponent.getAddress(name, password, authorityType)

        assertEquals(firstAddress, secondAddress)

        val secondName = "secondTestName"
        val secondPassword = "secondTestPassword"

        val thirdAddress = cryptoCoreComponent.getAddress(secondName, secondPassword, authorityType)

        assertNotEquals(firstAddress, thirdAddress)

        val firstAccountAddress = Address(firstAddress, network)
        val secondAccountAddress = Address(thirdAddress, network)

        assertNotEquals(firstAccountAddress.pubKey, secondAccountAddress.pubKey)
    }

    @Test
    fun privateKeyTest() {
        val privateKey = cryptoCoreComponent.getPrivateKey(name, password, authorityType)

        assertTrue(ECKey.fromPrivate(privateKey).hasPrivKey())
    }

    @Test
    fun signatureLengthTest() {
        val privateKey = cryptoCoreComponent.getPrivateKey(name, password, authorityType)

        val transferOperation = buildOperation()
        val transaction = Transaction(
            BlockData(5, 123L, 2342355235L),
            listOf(transferOperation),
            "39f5e2ede1f8bc1a3a54a7914414e3779e33193f1f5693510e73cb7a87617447"
        ).apply { addPrivateKey(privateKey) }

        val signature = cryptoCoreComponent.signTransaction(transaction)

        assertTrue(signature[0].size == SIGN_DATA_BYTES)
    }

    @Test
    fun encryptMessageTest() {
        assertNotNull(encrypt())
    }

    @Test
    fun decryptMessageTest() {
        val encrypted = encrypt()

        val privateKey =
            cryptoCoreComponent.getPrivateKey(secondName, secondPassword, AuthorityType.ACTIVE)
        val publicKey = Address(
            cryptoCoreComponent.getAddress(
                name,
                password,
                AuthorityType.ACTIVE
            ), network
        ).pubKey.key

        val decrypted =
            cryptoCoreComponent.decryptMessage(privateKey, publicKey, BigInteger.ZERO, encrypted)

        assertEquals(decrypted, message)
    }

    @Test
    fun decryptFailTest() {
        val encrypted = encrypt()

        val privateKey =
            cryptoCoreComponent.getPrivateKey("wrongName", "wrongPassword", AuthorityType.ACTIVE)
        val publicKey = Address(
            cryptoCoreComponent.getAddress(
                "wrongName1",
                "wrongPassword1",
                AuthorityType.ACTIVE
            ), network
        ).pubKey.key

        val decrypted =
            cryptoCoreComponent.decryptMessage(privateKey, publicKey, BigInteger.ZERO, encrypted)

        assertNotEquals(decrypted, message)
    }

    private fun encrypt(): ByteArray {
        val privateKey = cryptoCoreComponent.getPrivateKey(name, password, AuthorityType.ACTIVE)
        val publicKey = Address(
            cryptoCoreComponent.getAddress(
                secondName,
                secondPassword,
                AuthorityType.ACTIVE
            ), network
        ).pubKey.key

        val encrypted =
            cryptoCoreComponent.encryptMessage(privateKey, publicKey, BigInteger.ZERO, message)

        return encrypted!!
    }

    private fun buildOperation(): TransferOperation {
        val fee = AssetAmount(UnsignedLong.ONE)
        val fromAccount = Account("1.2.23215")
        val toAccount = Account("1.2.23216")
        val amount = AssetAmount(UnsignedLong.valueOf(10000), Asset("1.3.0"))

        return TransferOperationBuilder()
            .setFrom(fromAccount)
            .setTo(toAccount)
            .setFee(fee)
            .setAmount(amount)
            .build()
    }

}
