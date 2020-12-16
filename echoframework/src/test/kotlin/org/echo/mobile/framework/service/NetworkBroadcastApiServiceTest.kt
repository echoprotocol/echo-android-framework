package org.echo.mobile.framework.service

import com.google.common.primitives.UnsignedLong
import org.echo.mobile.framework.core.crypto.internal.CryptoCoreComponentImpl
import org.echo.mobile.framework.core.crypto.internal.eddsa.EdDSASecurityProvider
import org.echo.mobile.framework.core.crypto.internal.eddsa.key.NaCLKeyPairCryptoAdapter
import org.echo.mobile.framework.exception.LocalException
import org.echo.mobile.framework.model.*
import org.echo.mobile.framework.model.operations.TransferOperationBuilder
import org.echo.mobile.framework.service.internal.NetworkBroadcastApiServiceImpl
import org.echo.mobile.framework.support.error
import org.echo.mobile.framework.support.value
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.spongycastle.util.encoders.Hex
import java.security.Security

/**
 * Test cases for [NetworkBroadcastApiService]
 *
 * @author Dmitriy Bushuev
 */
class NetworkBroadcastApiServiceTest {

    private val cryptoCoreComponent =
        CryptoCoreComponentImpl(NaCLKeyPairCryptoAdapter())

    private lateinit var transaction: Transaction

    @Before
    fun setUp() {
        val transfer = TransferOperationBuilder()
            .setFrom(Account("1.2.18"))
            .setTo(Account("1.2.18"))
            .setAmount(AssetAmount(UnsignedLong.ONE, Asset("1.3.0")))
            .build()

        val blockData = BlockData(1, 1, 1)
        val chainId = Hex.toHexString("chainId".toByteArray())

        val privateKey =
            cryptoCoreComponent.getEdDSAPrivateKey()

        transaction = Transaction(
            blockData,
            listOf(transfer),
            chainId
        ).apply { addPrivateKey(privateKey) }

        Security.addProvider(EdDSASecurityProvider())
    }

    @Test
    fun broadcastTransactionWithCallbackTest() {
        val socketCoreComponent = ServiceSocketCoreComponentMock(true)

        val networkBroadcastApiService =
            NetworkBroadcastApiServiceImpl(socketCoreComponent, cryptoCoreComponent)

        networkBroadcastApiService.broadcastTransaction(transaction)
            .value { /*assertTrue(it)*/ }
            .error { fail() }
    }

    @Test
    fun broadcastTransactionWithCallbackErrorResultTest() {
        val socketCoreComponent = ServiceSocketCoreComponentMock(false)

        val networkBroadcastApiService =
            NetworkBroadcastApiServiceImpl(socketCoreComponent, cryptoCoreComponent)

        networkBroadcastApiService.broadcastTransaction(transaction)
            .value { /*assertFalse(it)*/ }
            .error { fail() }
    }

    @Test
    fun broadcastTransactionWithCallbackErrorTest() {
        val socketCoreComponent = ServiceSocketCoreComponentMock(null)

        val networkBroadcastApiService =
            NetworkBroadcastApiServiceImpl(socketCoreComponent, cryptoCoreComponent)

        networkBroadcastApiService.broadcastTransaction(transaction)
            .value { fail() }
            .error {
                assertNotNull(it)
                assertTrue(it is LocalException)
            }
    }

}
