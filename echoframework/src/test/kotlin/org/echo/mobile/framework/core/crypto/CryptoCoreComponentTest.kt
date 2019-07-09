package org.echo.mobile.framework.core.crypto

import com.google.common.primitives.UnsignedLong
import org.echo.mobile.framework.core.crypto.internal.CryptoCoreComponentImpl
import org.echo.mobile.framework.core.crypto.internal.eddsa.EdDSASecurityProvider
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
import org.echo.mobile.framework.support.crypto.EdDSASignature.SIGN_DATA_BYTES
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import java.security.Security

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

    private val message = "testMessage"

    @Before
    fun setUp() {
        network = Echodevnet()
        cryptoCoreComponent = CryptoCoreComponentImpl(NaCLKeyPairCryptoAdapter())

        Security.addProvider(EdDSASecurityProvider())
    }

    @Test
    fun addressesIsEqual() {
        val privateKey = cryptoCoreComponent.getEdDSAPrivateKey()
        val public = cryptoCoreComponent.deriveEdDSAPublicKeyFromPrivate(privateKey)

        val firstAddress = cryptoCoreComponent.getEdDSAAddressFromPublicKey(public)
        val secondAddress = cryptoCoreComponent.getEdDSAAddressFromPublicKey(public)

        assertEquals(firstAddress, secondAddress)

        val secondPrivate = cryptoCoreComponent.getEdDSAPrivateKey()
        val secondPublic = cryptoCoreComponent.deriveEdDSAPublicKeyFromPrivate(secondPrivate)

        val thirdAddress = cryptoCoreComponent.getEdDSAAddressFromPublicKey(secondPublic)

        assertNotEquals(firstAddress, thirdAddress)

        val firstAccountAddress = Address(firstAddress, network)
        val secondAccountAddress = Address(thirdAddress, network)

        assertNotEquals(firstAccountAddress.pubKey, secondAccountAddress.pubKey)
    }

    @Test
    fun edDSAAddressesIsEqual() {
        val privateKey = cryptoCoreComponent.getEdDSAPrivateKey()
        val public = cryptoCoreComponent.deriveEdDSAPublicKeyFromPrivate(privateKey)

        val firstAddress = cryptoCoreComponent.getEdDSAAddressFromPublicKey(public)
        val secondAddress = cryptoCoreComponent.getEdDSAAddressFromPublicKey(public)

        assertEquals(firstAddress, secondAddress)

        val secondPrivate = cryptoCoreComponent.getEdDSAPrivateKey()
        val secondPublic = cryptoCoreComponent.deriveEdDSAPublicKeyFromPrivate(secondPrivate)

        val thirdAddress = cryptoCoreComponent.getEdDSAAddressFromPublicKey(secondPublic)

        assertNotEquals(firstAddress, thirdAddress)

        val firstAccountAddress = Address(firstAddress, network)
        val secondAccountAddress = Address(thirdAddress, network)

        assertNotEquals(firstAccountAddress.pubKey, secondAccountAddress.pubKey)
    }

    @Test
    fun signatureLengthTest() {
        val privateKey = cryptoCoreComponent.getEdDSAPrivateKey()

        val transferOperation = buildOperation()
        val transaction = Transaction(
            BlockData(5, 123L, 2342355235L),
            listOf(transferOperation),
            "39f5e2ede1f8bc1a3a54a7914414e3779e33193f1f5693510e73cb7a87617447"
        ).apply { addPrivateKey(privateKey) }

        val signature = cryptoCoreComponent.signTransaction(transaction)

        assertEquals(SIGN_DATA_BYTES, signature[0].size)
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

    @Test
    fun decodeToWifSuccess() {
        val wifKey = "5J9YnfSUx6GnweorDEswRNAFcBzsZrQoJLkfqKLzXwBdRvjmoz1"

        val decodedWif = cryptoCoreComponent.decodeFromWif(wifKey).joinToString()

        assertEquals(
            "43, -68, -75, 90, 0, -125, 40, -6, -41, -101, -37, -55, 30, -71, 99, -92, 3, 40, -86, 60, -35, -12, 71, -68, 73, -122, -65, 69, 117, -109, 19, -20",
            decodedWif
        )
    }

}
