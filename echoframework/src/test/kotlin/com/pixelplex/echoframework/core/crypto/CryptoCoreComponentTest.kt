package com.pixelplex.echoframework.core.crypto

import com.google.common.primitives.UnsignedLong
import com.pixelplex.bitcoinj.ECKey
import com.pixelplex.echoframework.core.crypto.internal.CryptoCoreComponentImpl
import com.pixelplex.echoframework.model.*
import com.pixelplex.echoframework.model.network.Network
import com.pixelplex.echoframework.model.network.Testnet
import com.pixelplex.echoframework.model.operations.TransferOperation
import com.pixelplex.echoframework.model.operations.TransferOperationBuilder
import com.pixelplex.echoframework.support.Signature.SIGN_DATA_BYTES
import org.junit.Assert
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Test cases for [CryptoCoreComponentImpl]
 *
 * @author Dmitriy Bushuev
 */
class CryptoCoreComponentTest {

    private lateinit var cryptoCoreComponent: CryptoCoreComponent

    private val name = "testName"
    private val password = "testPassword"
    private lateinit var network: Network
    private val authorityType = AuthorityType.ACTIVE

    @Before
    fun setUp() {
        network = Testnet()
        cryptoCoreComponent = CryptoCoreComponentImpl(network)
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
            privateKey,
            BlockData(5, 123L, 2342355235L),
            listOf(transferOperation),
            "39f5e2ede1f8bc1a3a54a7914414e3779e33193f1f5693510e73cb7a87617447"
        )

        val signature = cryptoCoreComponent.signTransaction(transaction)

        Assert.assertTrue(signature.size == SIGN_DATA_BYTES)
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
