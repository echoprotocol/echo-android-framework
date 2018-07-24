package com.pixelplex.echoframework.support

import com.pixelplex.bitcoinj.ECKey
import com.pixelplex.echoframework.DEFAULT_URL
import com.pixelplex.echoframework.core.crypto.CryptoCoreComponent
import com.pixelplex.echoframework.core.crypto.internal.CryptoCoreComponentImpl
import com.pixelplex.echoframework.core.socket.SocketMessenger
import com.pixelplex.echoframework.core.socket.SocketMessengerListener
import com.pixelplex.echoframework.core.socket.internal.SocketMessengerImpl
import com.pixelplex.echoframework.model.AuthorityType
import com.pixelplex.echoframework.model.Transaction
import com.pixelplex.echoframework.model.network.Mainnet
import com.pixelplex.echoframework.model.network.Testnet
import org.junit.Assert.*
import org.junit.Test
import java.math.BigInteger

/**
 * Test cases for [Settings]
 *
 * @author Dmitriy Bushuev
 */
class SettingsTest {

    @Test
    fun defaultConfigurationTest() {
        val settings = Settings.Configurator().configure()

        assertEquals(settings.url, DEFAULT_URL)
        assertTrue(settings.socketMessenger is SocketMessengerImpl)
        assertTrue(settings.cryptoComponent is CryptoCoreComponentImpl)
        assertTrue(settings.apis.size == Api.values().size)
        assertFalse(settings.returnOnMainThread)
        assertTrue(settings.network is Testnet)
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

        override fun getAddress(
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

        override fun signTransaction(transaction: Transaction): ByteArray = ByteArray(1)

    }

}
