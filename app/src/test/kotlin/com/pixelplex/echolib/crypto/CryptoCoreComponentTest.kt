package com.pixelplex.echolib.crypto

import com.pixelplex.echolib.core.crypto.CryptoCoreComponent
import com.pixelplex.echolib.core.crypto.internal.CryptoCoreComponentImpl
import com.pixelplex.echolib.model.Address
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

    @Before
    fun setUp() {
        cryptoCoreComponent = CryptoCoreComponentImpl()
    }

    @Test
    fun addressEqualityTest() {
        val firstAddress = cryptoCoreComponent.getAddress(name, password)
        val secondAddress = cryptoCoreComponent.getAddress(name, password)

        assertEquals(firstAddress, secondAddress)

        val secondName = "secondTestName"
        val secondPassword = "secondTestPassword"

        val thirdAddress = cryptoCoreComponent.getAddress(secondName, secondPassword)

        assertNotEquals(firstAddress, thirdAddress)

        val firstAccountAddress = Address(firstAddress)
        val secondAccountAddress = Address(thirdAddress)

        assertNotEquals(firstAccountAddress.pubKey, secondAccountAddress.pubKey)
    }

    @Test
    fun privateKeyTest() {
        val privateKey = cryptoCoreComponent.getPrivateKey(name, password)

        assertTrue(privateKey.hasPrivKey())
    }

}
