package org.echo.mobile.framework.support

import org.echo.mobile.bitcoinj.ECKey
import org.echo.mobile.framework.ECHO_URL
import org.echo.mobile.framework.core.crypto.CryptoCoreComponent
import org.echo.mobile.framework.core.crypto.internal.CryptoCoreComponentImpl
import org.echo.mobile.framework.core.socket.SocketMessenger
import org.echo.mobile.framework.core.socket.SocketMessengerListener
import org.echo.mobile.framework.core.socket.internal.SocketMessengerImpl
import org.echo.mobile.framework.exception.LocalException
import org.echo.mobile.framework.model.AuthorityType
import org.echo.mobile.framework.model.Transaction
import org.echo.mobile.framework.model.network.Echodevnet
import org.echo.mobile.framework.model.network.Mainnet
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigInteger
import java.util.ArrayList

/**
 * Test cases for [Settings]
 *
 * @author Dmitriy Bushuev
 */
class SettingsTest {

    @Test
    fun defaultConfigurationTest() {
        val settings = Settings.Configurator().setUrl(ECHO_URL).configure()

        assertEquals(settings.url, ECHO_URL)
        assertTrue(settings.socketMessenger is SocketMessengerImpl)
        assertTrue(settings.cryptoComponent is CryptoCoreComponentImpl)
        assertTrue(settings.apis.size == Api.values().size)
        assertFalse(settings.returnOnMainThread)
        assertTrue(settings.network is Echodevnet)
    }

    @Test(expected = LocalException::class)
    fun defaultConfigurationFailureTest() {
        val settings = Settings.Configurator().configure()

        assertEquals(settings.url, ECHO_URL)
        assertTrue(settings.socketMessenger is SocketMessengerImpl)
        assertTrue(settings.cryptoComponent is CryptoCoreComponentImpl)
        assertTrue(settings.apis.size == Api.values().size)
        assertFalse(settings.returnOnMainThread)
        assertTrue(settings.network is Echodevnet)
    }

    @Test
    fun customConfigurationTest() {
        val url = "testUrl"
        val socketMessenger = TestSocketMessenger()
        val cryptoComponent = TestCryptoComponent()
        val apis = arrayOf(Api.NETWORK_BROADCAST, Api.DATABASE)
        val returnOnMainThread = true
        val network = Mainnet()

        val settings = Settings.Configurator().setUrl(url).setSocketMessenger(socketMessenger)
            .setCryptoCoreComponent(cryptoComponent).setApis(*apis)
            .setReturnOnMainThread(returnOnMainThread).setNetworkType(network).configure()

        assertEquals(settings.url, url)
        assertTrue(settings.socketMessenger == socketMessenger)
        assertTrue(settings.cryptoComponent == cryptoComponent)
        assertTrue(settings.apis.containsAll(apis.toSet()))
        assertTrue(settings.returnOnMainThread)
        assertTrue(settings.network == network)
    }

    private class TestSocketMessenger : SocketMessenger {
        override fun setUrl(url: String) {
        }

        override fun connect() {
        }

        override fun disconnect() {
        }

        override fun emit(message: String) {
        }

        override fun on(listener: SocketMessengerListener) {
        }

        override fun off(listener: SocketMessengerListener) {
        }

        override fun offAll() {
        }

    }

    private class TestCryptoComponent : CryptoCoreComponent {
        override fun derivePublicKeyFromPrivate(privateKey: ByteArray): ByteArray = byteArrayOf(1)

        override fun deriveEdDSAPublicKeyFromPrivate(privateKey: ByteArray): ByteArray = byteArrayOf(1)

        override fun getAddressFromPublicKey(publicKey: ByteArray): String = ""

        override fun getEdDSAAddressFromPublicKey(publicKey: ByteArray): String = ""

        override fun encodeToWif(source: ByteArray): String = ""

        override fun decodeFromWif(source: String): ByteArray = byteArrayOf(1)

        override fun getRawEchorandKey(userName: String, password: String): ByteArray =
            byteArrayOf(0)

        override fun getEchorandKey(userName: String, password: String): String = ""

        override fun encryptMessage(
            privateKey: ByteArray,
            publicKey: ByteArray,
            nonce: BigInteger,
            message: String
        ): ByteArray? = ByteArray(0)

        override fun decryptMessage(
            privateKey: ByteArray,
            publicKey: ByteArray,
            nonce: BigInteger,
            message: ByteArray
        ): String = ""

        override fun getAddress(
            userName: String,
            password: String,
            authorityType: AuthorityType
        ): String = "test"

        override fun getEdDSAAddress(
            userName: String,
            password: String,
            authorityType: AuthorityType
        ): String = "test"

        override fun getPrivateKey(
            userName: String,
            password: String,
            authorityType: AuthorityType
        ): ByteArray = ECKey.fromPrivate(
            BigInteger("0")
        ).getPrivKeyBytes()

        override fun getEdDSAPrivateKey(
            userName: String,
            password: String,
            authorityType: AuthorityType
        ): ByteArray = ECKey.fromPrivate(
            BigInteger("0")
        ).getPrivKeyBytes()

        override fun signTransaction(transaction: Transaction): ArrayList<ByteArray> =
            arrayListOf(ByteArray(1))

    }

}
