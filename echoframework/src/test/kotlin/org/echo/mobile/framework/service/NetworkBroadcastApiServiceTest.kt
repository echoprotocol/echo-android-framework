package org.echo.mobile.framework.service

import com.google.common.primitives.UnsignedLong
import org.echo.mobile.framework.core.crypto.internal.CryptoCoreComponentImpl
import org.echo.mobile.framework.exception.LocalException
import org.echo.mobile.framework.model.*
import org.echo.mobile.framework.model.network.Echodevnet
import org.echo.mobile.framework.model.operations.TransferOperationBuilder
import org.echo.mobile.framework.service.internal.NetworkBroadcastApiServiceImpl
import org.echo.mobile.framework.support.error
import org.echo.mobile.framework.support.value
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.spongycastle.util.encoders.Hex

/**
 * Test cases for [NetworkBroadcastApiService]
 *
 * @author Dmitriy Bushuev
 */
class NetworkBroadcastApiServiceTest {

    private val cryptoCoreComponent = CryptoCoreComponentImpl(Echodevnet())

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
            cryptoCoreComponent.getPrivateKey(
                "name",
                "password",
                AuthorityType.ACTIVE
            )

        transaction = Transaction(
            blockData,
            listOf(transfer),
            chainId
        ).apply { addPrivateKey(privateKey) }
    }

    @Test
    fun broadcastTransactionWithCallbackTest() {
        val socketCoreComponent = ServiceSocketCoreComponentMock(true)

        val networkBroadcastApiService =
            NetworkBroadcastApiServiceImpl(socketCoreComponent, cryptoCoreComponent)

        networkBroadcastApiService.broadcastTransaction(transaction)
            .value { assertTrue(it) }
            .error { fail() }
    }

    @Test
    fun broadcastTransactionWithCallbackErrorResultTest() {
        val socketCoreComponent = ServiceSocketCoreComponentMock(false)

        val networkBroadcastApiService =
            NetworkBroadcastApiServiceImpl(socketCoreComponent, cryptoCoreComponent)

        networkBroadcastApiService.broadcastTransaction(transaction)
            .value { assertFalse(it) }
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
