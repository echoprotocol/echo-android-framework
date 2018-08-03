package com.pixelplex.echoframework.service

import com.google.common.primitives.UnsignedLong
import com.pixelplex.echoframework.core.crypto.internal.CryptoCoreComponentImpl
import com.pixelplex.echoframework.exception.LocalException
import com.pixelplex.echoframework.model.*
import com.pixelplex.echoframework.model.network.Echodevnet
import com.pixelplex.echoframework.model.operations.TransferOperationBuilder
import com.pixelplex.echoframework.service.internal.NetworkBroadcastApiServiceImpl
import com.pixelplex.echoframework.support.error
import com.pixelplex.echoframework.support.value
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
            .setAmount(AssetAmount(UnsignedLong.valueOf(1), Asset("1.3.0")))
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
            privateKey,
            blockData,
            listOf(transfer),
            chainId
        )
    }

    @Test
    fun broadcastTransactionWithCallbackTest() {
        val socketCoreComponent = ServiceSocketCoreComponentMock(true)

        val networkBroadcastApiService =
            NetworkBroadcastApiServiceImpl(socketCoreComponent, cryptoCoreComponent)

        networkBroadcastApiService.broadcastTransactionWithCallback(transaction)
            .value { assertTrue(it) }
            .error { fail() }
    }

    @Test
    fun broadcastTransactionWithCallbackErrorResultTest() {
        val socketCoreComponent = ServiceSocketCoreComponentMock(false)

        val networkBroadcastApiService =
            NetworkBroadcastApiServiceImpl(socketCoreComponent, cryptoCoreComponent)

        networkBroadcastApiService.broadcastTransactionWithCallback(transaction)
            .value { assertFalse(it) }
            .error { fail() }
    }

    @Test
    fun broadcastTransactionWithCallbackErrorTest() {
        val socketCoreComponent = ServiceSocketCoreComponentMock(null)

        val networkBroadcastApiService =
            NetworkBroadcastApiServiceImpl(socketCoreComponent, cryptoCoreComponent)

        networkBroadcastApiService.broadcastTransactionWithCallback(transaction)
            .value { fail() }
            .error {
                assertNotNull(it)
                assertTrue(it is LocalException)
            }
    }

}
