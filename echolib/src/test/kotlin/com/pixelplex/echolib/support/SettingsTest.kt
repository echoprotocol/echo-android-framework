package com.pixelplex.echolib.support

import com.pixelplex.bitcoinj.ECKey
import com.pixelplex.echolib.DEFAULT_URL
import com.pixelplex.echolib.core.crypto.CryptoCoreComponent
import com.pixelplex.echolib.core.crypto.internal.CryptoCoreComponentImpl
import com.pixelplex.echolib.core.socket.SocketMessenger
import com.pixelplex.echolib.core.socket.SocketMessengerListener
import com.pixelplex.echolib.core.socket.internal.SocketMessengerImpl
import com.pixelplex.echolib.model.AuthorityType
import com.pixelplex.echolib.model.network.Mainnet
import com.pixelplex.echolib.model.network.Testnet
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
        ): ECKey = ECKey.fromPrivate(
            BigInteger("0")
        )

    }

}
